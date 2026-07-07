(function () {
  const content = document.getElementById('hrDashboardContent');

  if (!content || !window.fetch) {
    return;
  }

  loadDashboard();

  async function loadDashboard() {
    try {
      const [learning, courses, organizations] = await Promise.all([
        fetchJson('/hr/statistics/learning/data'),
        fetchJson('/hr/statistics/courses/data'),
        fetchJson('/hr/statistics/organizations/data'),
      ]);

      renderDashboard(learning, courses, organizations);
    } catch (error) {
      content.innerHTML = '<div class="hr-dashboard-loading">통계 데이터를 불러오지 못했습니다.</div>';
    }
  }

  async function fetchJson(url) {
    const response = await fetch(url, {
      headers: {
        Accept: 'application/json',
      },
    });

    if (!response.ok) {
      throw new Error(`Dashboard request failed: ${response.status}`);
    }

    return response.json();
  }

  function renderDashboard(learning, courses, organizations) {
    const charts = [];
    const kpiBlocks = [
      normalizeArray(learning.kpis)[0],
      normalizeArray(courses.kpis)[0],
      normalizeArray(learning.kpis)[1],
      normalizeArray(learning.kpis)[2],
    ].filter(Boolean);
    const monthlyStatus = findChartBlock(courses, 'course-monthly-learning-status', 2);
    const participationFunnel = findChartBlock(learning, 'learning-participation-funnel', 1);
    const departmentCompletion = findChartBlock(organizations, 'organization-department-completion', 1);
    const incompleteCourses = findRankBlock(courses, '미수료/이탈', 2);
    const lowParticipationDepartments = lowParticipationRankBlock(organizations);

    const fragments = ['<section class="hr-stats-section hr-stats-section--kpi">'];
    kpiBlocks.forEach((block) => fragments.push(renderBlock({ ...block, span: 1 })));
    fragments.push('</section>');

    fragments.push('<section class="hr-stats-section">');
    fragments.push('<h2 class="hr-stats-section-title">운영 요약</h2>');
    [monthlyStatus, participationFunnel, departmentCompletion].filter(Boolean).forEach((block) => {
      fragments.push(renderBlock(block));
      charts.push(block.chart);
    });
    fragments.push('</section>');

    fragments.push('<section class="hr-stats-section">');
    fragments.push('<h2 class="hr-stats-section-title">관리 필요</h2>');
    [incompleteCourses, lowParticipationDepartments].filter(Boolean).forEach((block) => {
      fragments.push(renderBlock(block));
    });
    fragments.push('</section>');

    content.innerHTML = fragments.join('');

    if (window.HrStatsCharts) {
      window.HrStatsCharts.render(charts);
    }
  }

  function findChartBlock(view, chartId, span) {
    for (const section of normalizeArray(view.sections)) {
      const block = normalizeArray(section.blocks).find((item) => item.kind === 'CHART' && item.chart?.id === chartId);
      if (block) {
        return { ...block, span };
      }
    }

    return null;
  }

  function findRankBlock(view, titlePart, span) {
    for (const section of normalizeArray(view.sections)) {
      const block = normalizeArray(section.blocks).find((item) => item.kind === 'RANK' && String(item.rankTitle || '').includes(titlePart));
      if (block) {
        return { ...block, span };
      }
    }

    return null;
  }

  function lowParticipationRankBlock(organizations) {
    const chart = findChartBlock(organizations, 'organization-department-participation', 1)?.chart;
    const points = normalizeArray(chart?.series?.[0]?.points);

    if (points.length === 0) {
      return null;
    }

    const ranks = [...points]
      .sort((a, b) => numericValue(a.value) - numericValue(b.value) || String(a.label || '').localeCompare(String(b.label || ''), 'ko'))
      .slice(0, 5)
      .map((point, index) => ({
        rank: index + 1,
        badgeText: '주의',
        badgeTone: index % 2 === 0 ? 'orange' : 'blue',
        label: point.label,
        value: point.displayValue || `${numericValue(point.value)}%`,
      }));

    return {
      kind: 'RANK',
      span: 2,
      rankTitle: '참여율 낮은 부서 랭킹',
      rankCaption: '참여율 기준',
      ranks,
    };
  }

  function renderBlock(block) {
    const spanClass = `span-${Math.min(Math.max(Number(block.span) || 1, 1), 4)}`;

    if (block.kind === 'CHART') {
      return renderChartBlock(block, spanClass);
    }

    if (block.kind === 'RANK') {
      return renderRankBlock(block, spanClass);
    }

    return renderKpiBlock(block, spanClass);
  }

  function renderKpiBlock(block, spanClass) {
    const kpi = block.kpi || {};
    const tone = kpi.tone ? ` ${escapeAttribute(kpi.tone)}` : '';

    return `
      <div class="hr-stats-block ${spanClass}">
        <article class="hr-stats-kpi">
          <span class="hr-stats-kpi__icon${tone}" aria-hidden="true">
            <i class="${escapeAttribute(kpi.icon || '')}"></i>
          </span>
          <div class="hr-stats-kpi__body">
            <p class="hr-stats-kpi__label">${escapeHtml(kpi.label)}</p>
            <p class="hr-stats-kpi__value">${escapeHtml(kpi.value)}</p>
            <p class="hr-stats-kpi__hint">${escapeHtml(kpi.hint)}</p>
          </div>
        </article>
      </div>
    `;
  }

  function renderChartBlock(block, spanClass) {
    const chart = block.chart || {};
    const id = escapeAttribute(chart.id || '');
    const type = escapeAttribute(chart.type || '');

    return `
      <div class="hr-stats-block ${spanClass}">
        <article class="hr-stats-card">
          <div class="hr-stats-card__header">
            <h2 class="hr-stats-card__title">${escapeHtml(chart.title)}</h2>
            <p class="hr-stats-card__caption">${escapeHtml(chart.caption)}</p>
          </div>
          <div class="hr-stats-chart ${type}" id="${id}" role="img" aria-label="${escapeAttribute(chart.title || '')}"></div>
        </article>
      </div>
    `;
  }

  function renderRankBlock(block, spanClass) {
    const ranks = normalizeArray(block.ranks);
    const rows = ranks.map((rank) => `
      <li>
        <span class="hr-stats-rank__meta">${escapeHtml(rank.rank)}</span>
        <span class="hr-stats-rank__badge badge rounded-pill ${escapeAttribute(rank.badgeTone || '')}">${escapeHtml(rank.badgeText)}</span>
        <span class="hr-stats-rank__label">${escapeHtml(rank.label)}</span>
        <strong>${escapeHtml(rank.value)}</strong>
      </li>
    `).join('');

    return `
      <div class="hr-stats-block ${spanClass}">
        <article class="hr-stats-card">
          <div class="hr-stats-card__header">
            <h2 class="hr-stats-card__title">${escapeHtml(block.rankTitle)}</h2>
            <p class="hr-stats-card__caption">${escapeHtml(block.rankCaption)}</p>
          </div>
          <ol class="hr-stats-rank">${rows}</ol>
        </article>
      </div>
    `;
  }

  function normalizeArray(value) {
    return Array.isArray(value) ? value : [];
  }

  function numericValue(value) {
    const number = Number(value);
    return Number.isFinite(number) ? number : 0;
  }

  function escapeHtml(value) {
    return String(value ?? '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function escapeAttribute(value) {
    return escapeHtml(value).replace(/`/g, '&#96;');
  }
})();
