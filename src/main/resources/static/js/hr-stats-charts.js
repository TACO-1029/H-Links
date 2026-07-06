(function () {
  const mountedCharts = new Map();

  const optionBuilders = {
    bar: buildBarOption,
    horizontalBar: buildHorizontalBarOption,
    line: buildLineOption,
    radar: buildRadarOption,
    donut: buildDonutOption,
    funnel: buildFunnelOption,
    heatmap: buildUnsupportedOption,
  };

  window.HrStatsCharts = {
    render,
    disposeAll,
  };

  const chartDataElement = document.getElementById('statsChartData');
  if (chartDataElement && window.echarts) {
    render(readInitialCharts(chartDataElement));
  }

  window.addEventListener('resize', () => {
    mountedCharts.forEach((instance) => instance.resize());
  });

  function render(charts) {
    if (!window.echarts || !Array.isArray(charts)) {
      return;
    }

    disposeAll();
    charts.forEach((chart) => mountChart(chart));
  }

  function disposeAll() {
    mountedCharts.forEach((instance) => instance.dispose());
    mountedCharts.clear();
  }

  function readInitialCharts(element) {
    try {
      return JSON.parse(element.textContent || '[]');
    } catch (error) {
      return [];
    }
  }

  function mountChart(chart) {
    const element = document.getElementById(chart.id);

    if (!element) {
      return;
    }

    const instance = window.echarts.init(element);
    const builder = optionBuilders[chart.type] || optionBuilders.bar;

    instance.setOption(builder(chart));
    mountedCharts.set(chart.id, instance);
  }

  function buildBaseOption(chart) {
    return {
      color: ['#009E7A', '#4F46E5', '#0891B2', '#8B5CF6'],
      animationDuration: 500,
      textStyle: {
        color: '#334155',
        fontFamily: 'Pretendard, Noto Sans KR, sans-serif',
      },
      tooltip: {
        trigger: 'axis',
        confine: true,
        backgroundColor: '#0F172A',
        borderWidth: 0,
        textStyle: {
          color: '#FFFFFF',
          fontSize: 12,
        },
        valueFormatter: (value) => formatValue(value, chart.unit),
      },
      grid: {
        top: 28,
        right: 18,
        bottom: 28,
        left: 44,
        containLabel: true,
      },
    };
  }

  function buildBarOption(chart) {
    const series = normalizeSeries(chart);
    const labels = extractLabels(series);
    const useCompactCategoryLabels = chart.id === 'course-popular-skills' || chart.id === 'course-rising-skills';

    return {
      ...buildBaseOption(chart),
      grid: useCompactCategoryLabels ? {
        top: 28,
        right: 18,
        bottom: 34,
        left: 44,
        containLabel: true,
      } : buildBaseOption(chart).grid,
      xAxis: {
        type: 'category',
        data: labels,
        axisTick: { show: false },
        axisLine: { lineStyle: { color: '#CBD5E1' } },
        axisLabel: {
          color: '#475569',
          fontSize: 12,
          interval: useCompactCategoryLabels ? 0 : 'auto',
          formatter: (value) => useCompactCategoryLabels ? truncateLabel(value, 7) : value,
        },
      },
      yAxis: {
        type: 'value',
        splitLine: { lineStyle: { color: '#E2E8F0' } },
        axisLabel: { color: '#475569', fontSize: 12 },
      },
      series: series.map((item) => ({
        name: item.name,
        type: 'bar',
        data: normalizePoints(item).map((point) => ({
          value: numericValue(point.value),
          displayValue: point.displayValue,
        })),
        barWidth: 16,
        label: {
          show: useCompactCategoryLabels,
          position: 'top',
          color: '#334155',
          fontSize: 11,
          formatter: (params) => params.data.displayValue || formatValue(params.value, chart.unit),
        },
        itemStyle: {
          borderRadius: [5, 5, 0, 0],
        },
        emphasis: {
          focus: 'series',
        },
      })),
    };
  }

  function buildHorizontalBarOption(chart) {
    const series = normalizeSeries(chart);
    const labels = extractLabels(series);

    return {
      ...buildBaseOption(chart),
      grid: {
        top: 24,
        right: 22,
        bottom: 18,
        left: 110,
        containLabel: true,
      },
      xAxis: {
        type: 'value',
        splitLine: { lineStyle: { color: '#E2E8F0' } },
        axisLabel: {
          color: '#475569',
          fontSize: 12,
          formatter: (value) => formatAxisValue(value, chart.unit),
        },
      },
      yAxis: {
        type: 'category',
        data: labels,
        inverse: true,
        axisTick: { show: false },
        axisLine: { lineStyle: { color: '#CBD5E1' } },
        axisLabel: {
          color: '#475569',
          fontSize: 12,
          width: 96,
          overflow: 'truncate',
        },
      },
      series: series.map((item) => ({
        name: item.name,
        type: 'bar',
        data: normalizePoints(item).map((point) => ({
          value: numericValue(point.value),
          displayValue: point.displayValue,
        })),
        barWidth: 14,
        label: {
          show: true,
          position: 'right',
          color: '#334155',
          fontSize: 11,
          formatter: (params) => params.data.displayValue || formatValue(params.value, chart.unit),
        },
        itemStyle: {
          borderRadius: [0, 5, 5, 0],
        },
        emphasis: {
          focus: 'series',
        },
      })),
    };
  }

  function buildLineOption(chart) {
    const series = normalizeSeries(chart);
    const labels = extractLabels(series);

    return {
      ...buildBaseOption(chart),
      xAxis: {
        type: 'category',
        data: labels,
        boundaryGap: false,
        axisTick: { show: false },
        axisLine: { lineStyle: { color: '#CBD5E1' } },
        axisLabel: { color: '#475569', fontSize: 12 },
      },
      yAxis: {
        type: 'value',
        splitLine: { lineStyle: { color: '#E2E8F0' } },
        axisLabel: {
          color: '#475569',
          fontSize: 12,
          formatter: (value) => formatAxisValue(value, chart.unit),
        },
      },
      series: series.map((item) => ({
        name: item.name,
        type: 'line',
        data: normalizePoints(item).map((point) => ({
          value: numericValue(point.value),
          displayValue: point.displayValue,
        })),
        smooth: true,
        symbol: 'circle',
        symbolSize: 7,
        lineStyle: {
          width: 3,
        },
        label: {
          show: true,
          position: 'top',
          color: '#334155',
          fontSize: 11,
          formatter: (params) => params.data.displayValue || formatValue(params.value, chart.unit),
        },
      })),
    };
  }

  function buildRadarOption(chart) {
    const series = normalizeSeries(chart);
    const firstSeries = series[0] || { name: '', points: [] };
    const points = normalizePoints(firstSeries);
    const useRightLegend = chart.id === 'organization-department-average-competency';
    const hideLegend = chart.id === 'learning-average-skill';
    const maxValue = Math.max(
      ...series.flatMap((item) => normalizePoints(item).map((point) => numericValue(point.value))),
      100
    );

    return {
      ...buildBaseOption(chart),
      tooltip: {
        trigger: 'item',
        confine: true,
      },
      radar: {
        radius: useRightLegend ? '60%' : '68%',
        center: useRightLegend ? ['38%', '54%'] : ['50%', '54%'],
        splitNumber: 4,
        axisName: {
          color: '#334155',
          fontSize: 12,
          lineHeight: 16,
        },
        axisNameGap: useRightLegend ? 18 : 15,
        splitLine: {
          lineStyle: { color: '#CBD5E1' },
        },
        splitArea: {
          areaStyle: {
            color: ['rgba(0, 158, 122, 0.03)', 'rgba(0, 158, 122, 0.07)'],
          },
        },
        axisLine: {
          lineStyle: { color: '#CBD5E1' },
        },
        indicator: points.map((point) => ({
          name: point.label,
          max: Math.max(100, maxValue),
        })),
      },
      legend: {
        show: !hideLegend,
        bottom: useRightLegend ? 'middle' : 0,
        right: useRightLegend ? 0 : undefined,
        orient: useRightLegend ? 'vertical' : 'horizontal',
        type: 'scroll',
        itemWidth: 10,
        itemHeight: 10,
        textStyle: {
          color: '#475569',
          fontSize: 12,
        },
      },
      series: [{
        type: 'radar',
        data: series.map((item) => ({
          value: normalizePoints(item).map((point) => numericValue(point.value)),
          name: item.name,
          areaStyle: {
            opacity: 0.12,
          },
        })),
        lineStyle: {
          width: 2,
        },
      }],
    };
  }

  function buildDonutOption(chart) {
    const series = normalizeSeries(chart);
    const firstSeries = series[0] || { name: '', points: [] };
    const points = normalizePoints(firstSeries);

    return {
      ...buildBaseOption(chart),
      tooltip: {
        trigger: 'item',
        confine: true,
        backgroundColor: '#0F172A',
        borderWidth: 0,
        textStyle: {
          color: '#FFFFFF',
          fontSize: 12,
        },
        formatter: (params) => `${params.name}: ${formatValue(params.value, chart.unit)}`,
      },
      legend: {
        orient: 'vertical',
        right: 0,
        top: 'middle',
        itemWidth: 10,
        itemHeight: 10,
        textStyle: {
          color: '#475569',
          fontSize: 12,
        },
      },
      grid: undefined,
      series: [{
        name: firstSeries.name,
        type: 'pie',
        radius: ['48%', '72%'],
        center: ['38%', '52%'],
        avoidLabelOverlap: true,
        data: points.map((point) => ({
          name: point.label,
          value: numericValue(point.value),
          displayValue: point.displayValue,
        })),
        label: {
          color: '#334155',
          fontSize: 12,
          formatter: (params) => `${params.name}\n${formatValue(params.value, chart.unit)}`,
        },
        labelLine: {
          length: 10,
          length2: 8,
        },
      }],
    };
  }

  function buildFunnelOption(chart) {
    const series = normalizeSeries(chart);
    const firstSeries = series[0] || { name: '', points: [] };
    const points = normalizePoints(firstSeries);

    return {
      ...buildBaseOption(chart),
      tooltip: {
        trigger: 'item',
        confine: true,
        backgroundColor: '#0F172A',
        borderWidth: 0,
        textStyle: {
          color: '#FFFFFF',
          fontSize: 12,
        },
        formatter: (params) => `${params.name}: ${formatValue(params.value, chart.unit)}`,
      },
      legend: {
        show: false,
      },
      grid: undefined,
      series: [{
        name: firstSeries.name,
        type: 'funnel',
        left: '8%',
        top: 12,
        width: '84%',
        height: '88%',
        min: 0,
        max: Math.max(...points.map((point) => numericValue(point.value)), 100),
        minSize: '20%',
        maxSize: '100%',
        sort: 'none',
        gap: 3,
        label: {
          show: true,
          position: 'inside',
          color: '#FFFFFF',
          fontSize: 12,
          fontWeight: 700,
          formatter: (params) => `${params.name} ${formatValue(params.value, chart.unit)}`,
        },
        labelLine: {
          show: false,
        },
        itemStyle: {
          borderColor: '#FFFFFF',
          borderWidth: 1,
        },
        emphasis: {
          label: {
            fontSize: 13,
          },
        },
        data: points.map((point) => ({
          name: point.label,
          value: numericValue(point.value),
          displayValue: point.displayValue,
        })),
      }],
    };
  }

  function buildUnsupportedOption(chart) {
    return {
      title: {
        text: `${chart.type} 차트 준비 중`,
        left: 'center',
        top: 'middle',
        textStyle: {
          color: '#64748B',
          fontSize: 14,
          fontWeight: 600,
        },
      },
    };
  }

  function normalizeSeries(chart) {
    return Array.isArray(chart.series) ? chart.series : [];
  }

  function normalizePoints(series) {
    return Array.isArray(series.points) ? series.points : [];
  }

  function extractLabels(series) {
    const firstSeries = series[0];
    return firstSeries ? normalizePoints(firstSeries).map((point) => point.label) : [];
  }

  function numericValue(value) {
    const number = Number(value);
    return Number.isFinite(number) ? number : 0;
  }

  function formatValue(value, unit) {
    return `${Number(value).toLocaleString()}${unit || ''}`;
  }

  function truncateLabel(value, maxLength) {
    const text = String(value || '');
    return text.length > maxLength ? `${text.slice(0, maxLength)}...` : text;
  }

  function formatAxisValue(value, unit) {
    if (unit === 'h') {
      return `${Number(value).toLocaleString()}h`;
    }

    if (unit === '%') {
      return `${value}%`;
    }

    return Number(value).toLocaleString();
  }
})();
