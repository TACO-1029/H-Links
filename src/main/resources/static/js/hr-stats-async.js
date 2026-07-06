(function () {
  const form = document.querySelector('.hr-stats-filter');
  const content = document.getElementById('statsContent');

  if (!form) {
    return;
  }

  bindPeriodPresets(form);
  bindDateRangeDropdowns(form);
  bindOrganizationFilter(form);
  bindStatsAlert();
  updateDateRangeLabels(form);
  updatePeriodPresetState(form);

  form.querySelectorAll('input[name="startDate"], input[name="endDate"]').forEach((input) => {
    input.addEventListener('change', () => {
      updateDateRangeLabels(form);
      updatePeriodPresetState(form);
    });
  });

  if (!content || !window.fetch) {
    return;
  }

  form.addEventListener('submit', async (event) => {
    event.preventDefault();

    if (hasEmptyOrganizationSelection(form)) {
      showStatsAlert('통계를 확인할 부서를 선택해주세요.');
      return;
    }

    const submitButton = form.querySelector('[type="submit"]');
    setLoading(submitButton, true);

    try {
      const url = buildDataUrl(form);
      const response = await fetch(url, {
        headers: {
          Accept: 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error(`Statistics request failed: ${response.status}`);
      }

      const view = await response.json();
      renderStats(view);
      updatePeriodPresetState(form);
      updateAddressBar(form);
    } catch (error) {
      form.submit();
    } finally {
      setLoading(submitButton, false);
    }
  });

  function buildDataUrl(formElement) {
    const params = new URLSearchParams(new FormData(formElement));
    return `${window.location.pathname.replace(/\/$/, '')}/data?${params.toString()}`;
  }

  function updateAddressBar(formElement) {
    const params = new URLSearchParams(new FormData(formElement));
    const query = params.toString();
    const nextUrl = query ? `${window.location.pathname}?${query}` : window.location.pathname;
    window.history.replaceState(null, '', nextUrl);
  }

  function renderStats(view) {
    const charts = [];
    const fragments = ['<section class="hr-stats-section hr-stats-section--kpi">'];

    normalizeArray(view.kpis).forEach((block) => {
      fragments.push(renderBlock(block));
    });
    fragments.push('</section>');

    normalizeArray(view.sections).forEach((section) => {
      const blocks = normalizeArray(section.blocks);
      const sectionClass = blocks.length === 3 ? ' hr-stats-section--thirds' : '';

      fragments.push(`<section class="hr-stats-section${sectionClass}">`);
      fragments.push(`<h2 class="hr-stats-section-title">${escapeHtml(section.title)}</h2>`);
      blocks.forEach((block) => {
        fragments.push(renderBlock(block));
        if (block.kind === 'CHART' && block.chart) {
          charts.push(block.chart);
        }
      });
      fragments.push('</section>');
    });

    content.innerHTML = fragments.join('');

    if (window.HrStatsCharts) {
      window.HrStatsCharts.render(charts);
    }
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
          <div class="hr-stats-chart ${type}"
               id="${id}"
               data-chart-id="${id}"
               data-chart-type="${type}"
               role="img"
               aria-label="${escapeAttribute(chart.title || '')}">
          </div>
        </article>
      </div>
    `;
  }

  function renderRankBlock(block, spanClass) {
    const ranks = normalizeArray(block.ranks);
    const detailed = Boolean(ranks[0]?.categoryName || ranks[0]?.courseTypeName || ranks[0]?.completionRate);
    const rows = block.rankTable ? renderRankMatrixTable(block.rankTable) : detailed ? renderRankTable(ranks, block) : renderRankList(ranks);
    const emptyState = !block.rankTable && ranks.length === 0 ? renderRankEmptyState() : '';

    return `
      <div class="hr-stats-block ${spanClass}">
        <article class="hr-stats-card">
          <div class="hr-stats-card__header">
            <h2 class="hr-stats-card__title">${escapeHtml(block.rankTitle)}</h2>
            <p class="hr-stats-card__caption">${escapeHtml(block.rankCaption)}</p>
          </div>
          ${rows}
          ${emptyState}
        </article>
      </div>
    `;
  }

  function renderRankList(ranks) {
    const rows = ranks.map((rank) => `
      <li>
        <span class="hr-stats-rank__meta">${escapeHtml(rank.rank)}</span>
        ${rank.badgeText ? `<span class="hr-stats-rank__badge badge rounded-pill ${escapeAttribute(rank.badgeTone || '')}">${escapeHtml(rank.badgeText)}</span>` : ''}
        <span class="hr-stats-rank__label">${escapeHtml(rank.label)}</span>
        <strong>${escapeHtml(rank.value)}</strong>
      </li>
    `).join('');

    return `<ol class="hr-stats-rank">${rows}</ol>`;
  }

  function renderRankMatrixTable(rankTable) {
    const columns = normalizeArray(rankTable.columns);
    const rows = normalizeArray(rankTable.rows).map((row) => `
      <tr>
        <td class="hr-stats-matrix-table__rank">${escapeHtml(row.rankLabel)}</td>
        ${normalizeArray(row.values).map((value) => `
          <td>
            <span class="hr-stats-matrix-table__course" title="${escapeAttribute(value)}">${escapeHtml(value)}</span>
          </td>
        `).join('')}
      </tr>
    `).join('');

    return `
      <div class="hr-stats-matrix-wrap">
        <table class="hr-stats-matrix-table">
          <thead>
            <tr>
              <th class="hr-stats-matrix-table__rank">순위</th>
              ${columns.map((column) => `<th>${escapeHtml(column)}</th>`).join('')}
            </tr>
          </thead>
          <tbody>${rows}</tbody>
        </table>
      </div>
    `;
  }

  function renderRankEmptyState() {
    return `
      <div class="hr-stats-empty">
        <i class="bi bi-database-slash" aria-hidden="true"></i>
        <p>표시할 인기 강의가 없습니다.</p>
      </div>
    `;
  }

  function renderRankTable(ranks, block) {
    const countLabel = String(block.rankTitle || '').includes('미수료') ? '미수료/이탈' : '신청 수';
    const rows = ranks.map((rank) => `
      <tr>
        <td>${escapeHtml(rank.rank)}</td>
        <td class="hr-stats-table__title">${escapeHtml(rank.label)}</td>
        <td>${renderCourseType(rank)}</td>
        <td>${escapeHtml(rank.applicationCount)}</td>
        <td>${escapeHtml(rank.completionRate)}</td>
      </tr>
    `).join('');

    return `
      <div class="hr-stats-table-wrap">
        <table class="hr-stats-table hr-stats-table--compact">
          <thead>
            <tr>
              <th>순위</th>
              <th>강의명</th>
              <th>유형</th>
              <th>${countLabel}</th>
              <th>수료율</th>
            </tr>
          </thead>
          <tbody>${rows}</tbody>
        </table>
      </div>
    `;
  }

  function renderCourseType(rank) {
    if (rank.badgeText) {
      return `<span class="hr-stats-table__type badge rounded-pill ${escapeAttribute(rank.badgeTone || '')}">${escapeHtml(rank.badgeText)}</span>`;
    }

    return `<span class="hr-stats-table__type-text">${escapeHtml(rank.courseTypeName)}</span>`;
  }

  function setLoading(button, loading) {
    if (!button) {
      return;
    }

    button.disabled = loading;
    button.textContent = loading ? '조회 중' : '조회';
  }

  function bindPeriodPresets(formElement) {
    formElement.querySelectorAll('.hr-stats-filter__preset').forEach((button) => {
      button.addEventListener('click', () => {
        const range = calculatePresetRange(button);
        const startInput = formElement.querySelector('input[name="startDate"]');
        const endInput = formElement.querySelector('input[name="endDate"]');

        if (!range || !startInput || !endInput) {
          return;
        }

        startInput.value = formatDate(range.startDate);
        endInput.value = formatDate(range.endDate);
        updateDateRangeLabels(formElement);
        updatePeriodPresetState(formElement);
        closeDateRangeDropdowns(formElement);

        if (formElement.requestSubmit) {
          formElement.requestSubmit();
          return;
        }

        formElement.dispatchEvent(new Event('submit', { cancelable: true }));
      });
    });
  }

  function bindDateRangeDropdowns(formElement) {
    formElement.querySelectorAll('[data-date-range]').forEach((dateRange) => {
      const toggle = dateRange.querySelector('[data-date-range-toggle]');

      if (!toggle) {
        return;
      }

      toggle.addEventListener('click', () => {
        const nextOpen = !dateRange.classList.contains('open');
        closeDateRangeDropdowns(formElement);
        setDateRangeOpen(dateRange, nextOpen);
      });
    });

    document.addEventListener('click', (event) => {
      if (!formElement.contains(event.target)) {
        closeDateRangeDropdowns(formElement);
      }
    });

    document.addEventListener('keydown', (event) => {
      if (event.key === 'Escape') {
        closeDateRangeDropdowns(formElement);
      }
    });
  }

  function bindOrganizationFilter(formElement) {
    const picker = formElement.querySelector('[data-org-filter]');

    if (!picker) {
      return;
    }

    const toggle = picker.querySelector('[data-org-filter-toggle]');
    const label = picker.querySelector('[data-org-filter-label]');
    const tree = picker.querySelector('.hr-stats-org-tree');
    const maxSelection = Number(picker.dataset.maxSelection) || 5;
    const items = Array.from(picker.querySelectorAll('[data-department-id]')).map((element) => ({
      element,
      input: element.querySelector('input[type="checkbox"]'),
      id: element.dataset.departmentId,
      parentId: element.dataset.parentId || '',
      name: element.querySelector('.hr-stats-org-tree__name')?.textContent?.trim() || '',
    }));

    if (!toggle || items.length === 0) {
      return;
    }

    const itemsById = new Map(items.map((item) => [item.id, item]));
    const childrenByParentId = items.reduce((map, item) => {
      if (!map.has(item.parentId)) {
        map.set(item.parentId, []);
      }
      map.get(item.parentId).push(item);
      return map;
    }, new Map());

    toggle.addEventListener('click', () => {
      const nextOpen = !picker.classList.contains('open');
      closeOrganizationFilters(formElement);
      setOrganizationFilterOpen(picker, nextOpen);
    });

    items.forEach((item) => {
      item.input.addEventListener('change', () => {
        const previousScrollTop = tree ? tree.scrollTop : 0;

        if (item.input.checked) {
          uncheckDescendants(item);
          uncheckAncestors(item);
        }

        if (checkedItems().length > maxSelection) {
          item.input.checked = false;
          window.alert(`비교 조직은 최대 ${maxSelection}개까지 선택할 수 있습니다.`);
        }

        updateIndeterminateStates();
        updateOrganizationFilterLabel();

        if (tree) {
          tree.scrollTop = previousScrollTop;
        }
      });
    });

    document.addEventListener('click', (event) => {
      if (!picker.contains(event.target)) {
        setOrganizationFilterOpen(picker, false);
      }
    });

    document.addEventListener('keydown', (event) => {
      if (event.key === 'Escape') {
        setOrganizationFilterOpen(picker, false);
      }
    });

    updateIndeterminateStates();
    updateOrganizationFilterLabel();

    function checkedItems() {
      return items.filter((item) => item.input.checked);
    }

    function uncheckDescendants(item) {
      getDescendants(item).forEach((descendant) => {
        descendant.input.checked = false;
        descendant.input.indeterminate = false;
      });
    }

    function uncheckAncestors(item) {
      getAncestors(item).forEach((ancestor) => {
        ancestor.input.checked = false;
      });
    }

    function getDescendants(item) {
      const children = childrenByParentId.get(item.id) || [];
      return children.flatMap((child) => [child, ...getDescendants(child)]);
    }

    function getAncestors(item) {
      const parent = itemsById.get(item.parentId);

      if (!parent) {
        return [];
      }

      return [parent, ...getAncestors(parent)];
    }

    function updateIndeterminateStates() {
      items.forEach((item) => {
        item.input.indeterminate = !item.input.checked && hasSelectedDescendant(item);
      });
    }

    function hasSelectedDescendant(item) {
      return getDescendants(item).some((descendant) => descendant.input.checked);
    }

    function updateOrganizationFilterLabel() {
      if (!label) {
        return;
      }

      const selectedItems = checkedItems();

      if (selectedItems.length === 1) {
        label.textContent = selectedItems[0].name;
        return;
      }

      label.textContent = `비교 부서 ${selectedItems.length}개`;
    }
  }

  function closeOrganizationFilters(formElement) {
    formElement.querySelectorAll('[data-org-filter].open').forEach((picker) => {
      setOrganizationFilterOpen(picker, false);
    });
  }

  function setOrganizationFilterOpen(picker, open) {
    const toggle = picker.querySelector('[data-org-filter-toggle]');

    picker.classList.toggle('open', open);
    if (toggle) {
      toggle.setAttribute('aria-expanded', open ? 'true' : 'false');
    }
  }

  function hasEmptyOrganizationSelection(formElement) {
    const picker = formElement.querySelector('[data-org-filter]');

    if (!picker) {
      return false;
    }

    return !Array.from(picker.querySelectorAll('input[name="departmentIds"]'))
      .some((input) => input.checked);
  }

  function bindStatsAlert() {
    const modal = document.getElementById('hrStatsAlertModal');
    const closeButton = document.getElementById('hrStatsAlertClose');

    if (!modal || !closeButton) {
      return;
    }

    closeButton.addEventListener('click', hideStatsAlert);

    modal.addEventListener('click', (event) => {
      if (event.target === modal) {
        hideStatsAlert();
      }
    });

    document.addEventListener('keydown', (event) => {
      if (event.key === 'Escape') {
        hideStatsAlert();
      }
    });
  }

  function showStatsAlert(message) {
    const modal = document.getElementById('hrStatsAlertModal');
    const messageElement = document.getElementById('hrStatsAlertMessage');
    const closeButton = document.getElementById('hrStatsAlertClose');

    if (!modal || !messageElement || !closeButton) {
      return;
    }

    messageElement.textContent = message;
    modal.classList.add('is-open');
    closeButton.focus();
  }

  function hideStatsAlert() {
    const modal = document.getElementById('hrStatsAlertModal');

    if (modal) {
      modal.classList.remove('is-open');
    }
  }

  function closeDateRangeDropdowns(formElement) {
    formElement.querySelectorAll('[data-date-range].open').forEach((dateRange) => {
      setDateRangeOpen(dateRange, false);
    });
  }

  function setDateRangeOpen(dateRange, open) {
    const toggle = dateRange.querySelector('[data-date-range-toggle]');

    dateRange.classList.toggle('open', open);
    if (toggle) {
      toggle.setAttribute('aria-expanded', open ? 'true' : 'false');
    }
  }

  function updateDateRangeLabels(formElement) {
    const startInput = formElement.querySelector('input[name="startDate"]');
    const endInput = formElement.querySelector('input[name="endDate"]');
    const label = formElement.querySelector('[data-date-range-label]');

    if (!startInput || !endInput || !label) {
      return;
    }

    label.textContent = `${startInput.value || '-'} ~ ${endInput.value || '-'}`;
  }

  function calculatePresetRange(button) {
    const endDate = today();
    const days = Number(button.dataset.periodDays);
    const months = Number(button.dataset.periodMonths);
    const startDate = new Date(endDate);

    if (button.hasAttribute('data-period-current-month')) {
      startDate.setDate(1);
      return { startDate, endDate };
    }

    if (button.hasAttribute('data-period-current-year')) {
      startDate.setMonth(0, 1);
      return { startDate, endDate };
    }

    if (Number.isFinite(days) && days > 0) {
      startDate.setDate(endDate.getDate() - days + 1);
      return { startDate, endDate };
    }

    if (Number.isFinite(months) && months > 0) {
      const previousDate = addMonths(endDate, -months);
      startDate.setFullYear(previousDate.getFullYear(), previousDate.getMonth(), previousDate.getDate());
      startDate.setDate(startDate.getDate() + 1);
      return { startDate, endDate };
    }

    return null;
  }

  function updatePeriodPresetState(formElement) {
    const startInput = formElement.querySelector('input[name="startDate"]');
    const endInput = formElement.querySelector('input[name="endDate"]');

    if (!startInput || !endInput) {
      return;
    }

    formElement.querySelectorAll('.hr-stats-filter__preset').forEach((button) => {
      const range = calculatePresetRange(button);
      const active = range
        && startInput.value === formatDate(range.startDate)
        && endInput.value === formatDate(range.endDate);

      button.classList.toggle('active', active);
      button.setAttribute('aria-pressed', active ? 'true' : 'false');
    });
  }

  function today() {
    const date = new Date();
    return new Date(date.getFullYear(), date.getMonth(), date.getDate());
  }

  function addMonths(date, amount) {
    const nextDate = new Date(date);
    const targetMonth = nextDate.getMonth() + amount;

    nextDate.setDate(1);
    nextDate.setMonth(targetMonth);
    nextDate.setDate(Math.min(date.getDate(), daysInMonth(nextDate.getFullYear(), nextDate.getMonth())));

    return nextDate;
  }

  function daysInMonth(year, month) {
    return new Date(year, month + 1, 0).getDate();
  }

  function formatDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  function normalizeArray(value) {
    return Array.isArray(value) ? value : [];
  }

  function escapeHtml(value) {
    return String(value ?? '')
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#039;');
  }

  function escapeAttribute(value) {
    return escapeHtml(value).replaceAll('`', '&#096;');
  }
})();
