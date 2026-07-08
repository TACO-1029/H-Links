package com.hlinks.domain.hr.controller;

import com.hlinks.domain.hr.dto.AdminDashboardFilter;
import com.hlinks.domain.hr.service.AdminDashboardService;
import com.hlinks.domain.quiz.dto.QuizListResponse;
import com.hlinks.domain.quiz.service.QuizService;
import com.hlinks.domain.statistics.service.StatisticsFilterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

@Controller
@RequiredArgsConstructor
public class HrController {

    private static final int COMPLETION_PAGE_SIZE = 10;

    private final QuizService quizService;
    private final AdminDashboardService adminDashboardService;
    private final StatisticsFilterService statisticsFilterService;

    @GetMapping("/hr")
    public String index(Model model) {
        model.addAttribute("activeMenu", "dashboard");
        return "hr/index";
    }

    @GetMapping("/hr/completions")
    public String courseCompletions(
            AdminDashboardFilter filter,
            @RequestParam(defaultValue = "1") int page,
            Model model
    ) {
        int totalCount = adminDashboardService.countCourseCompletions(filter);
        int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / COMPLETION_PAGE_SIZE));
        int currentPage = Math.max(1, Math.min(page, totalPages));
        int offset = (currentPage - 1) * COMPLETION_PAGE_SIZE;

        model.addAttribute("activeMenu", "course-completions");
        model.addAttribute("filter", filter);
        model.addAttribute("completions", adminDashboardService.getCourseCompletions(filter, offset, COMPLETION_PAGE_SIZE));
        model.addAttribute("departments", statisticsFilterService.getDepartmentOptions());
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("pageSize", COMPLETION_PAGE_SIZE);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageItems", buildPageItems(currentPage, totalPages));
        return "hr/completions";
    }

    @GetMapping("/hr/quizzes/ai")
    public String aiGeneratedQuizzes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String course,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            Model model
    ) {
        List<QuizListResponse> allQuizzes = quizService.getAiGeneratedQuizzes();
        List<String> courseOptions = allQuizzes.stream()
                .map(QuizListResponse::getCourseTitle)
                .filter(this::hasText)
                .distinct()
                .sorted()
                .toList();
        List<String> statusOptions = allQuizzes.stream()
                .map(QuizListResponse::getBuildStatusLabel)
                .filter(this::hasText)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();
        List<QuizListResponse> filteredQuizzes = allQuizzes.stream()
                .filter(quiz -> matches(quiz.getCourseTitle(), course))
                .filter(quiz -> matches(quiz.getBuildStatusLabel(), status))
                .filter(quiz -> matchesKeyword(quiz, keyword))
                .toList();
        int pageSize = Math.max(1, Math.min(size, 50));
        int totalCount = filteredQuizzes.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / pageSize));
        int currentPage = Math.max(1, Math.min(page, totalPages));
        int fromIndex = Math.min((currentPage - 1) * pageSize, totalCount);
        int toIndex = Math.min(fromIndex + pageSize, totalCount);
        List<QuizListResponse> pagedQuizzes = filteredQuizzes.subList(fromIndex, toIndex);
        List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                .boxed()
                .toList();

        model.addAttribute("activeMenu", "hr");
        model.addAttribute("quizzes", pagedQuizzes);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("courseOptions", courseOptions);
        model.addAttribute("statusOptions", statusOptions);
        model.addAttribute("courseFilter", normalize(course));
        model.addAttribute("statusFilter", normalize(status));
        model.addAttribute("keyword", normalize(keyword));
        return "hr/quizzes-ai";
    }

    private boolean matches(String value, String filter) {
        return !hasText(filter) || Objects.equals(normalize(value), normalize(filter));
    }

    private boolean matchesKeyword(QuizListResponse quiz, String keyword) {
        if (!hasText(keyword)) {
            return true;
        }

        String normalizedKeyword = normalize(keyword).toLowerCase();

        return contains(quiz.getDisplayQuestionText(), normalizedKeyword)
                || contains(quiz.getCourseTitle(), normalizedKeyword)
                || contains(quiz.getChapterTitle(), normalizedKeyword)
                || contains(quiz.getBuildStatusLabel(), normalizedKeyword)
                || contains(quiz.getReviewStatusLabel(), normalizedKeyword);
    }

    private boolean contains(String value, String keyword) {
        return hasText(value) && normalize(value).toLowerCase().contains(keyword);
    }

    private List<PageItem> buildPageItems(int currentPage, int totalPages) {
        if (totalPages <= 7) {
            return IntStream.rangeClosed(1, totalPages)
                    .mapToObj(page -> PageItem.page(page, page == currentPage))
                    .toList();
        }

        java.util.ArrayList<PageItem> items = new java.util.ArrayList<>();
        items.add(PageItem.page(1, currentPage == 1));

        int start = Math.max(2, currentPage - 2);
        int end = Math.min(totalPages - 1, currentPage + 2);

        if (currentPage <= 3) {
            start = 2;
            end = 5;
        } else if (currentPage >= totalPages - 2) {
            start = totalPages - 4;
            end = totalPages - 1;
        }

        if (start > 2) {
            items.add(PageItem.ellipsisItem());
        }

        for (int page = start; page <= end; page += 1) {
            items.add(PageItem.page(page, page == currentPage));
        }

        if (end < totalPages - 1) {
            items.add(PageItem.ellipsisItem());
        }

        items.add(PageItem.page(totalPages, currentPage == totalPages));
        return items;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    public record PageItem(Integer page, String label, boolean ellipsis, boolean active) {

        private static PageItem page(int page, boolean active) {
            return new PageItem(page, String.valueOf(page), false, active);
        }

        private static PageItem ellipsisItem() {
            return new PageItem(null, "...", true, false);
        }
    }
}
