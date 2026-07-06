(function () {
  const mountedCharts = new Map();

  const optionBuilders = {
    bar: buildBarOption,
    horizontalBar: buildHorizontalBarOption,
    line: buildLineOption,
    growthFactors: buildGrowthFactorsOption,
    radar: buildRadarOption,
    donut: buildDonutOption,
    funnel: buildFunnelOption,
    heatmap: buildHeatmapOption,
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
      color: [
        "#009E7A",
        "#F1A400",
        "#A0AB3B",
        "#4DA866"
      ],
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
          itemStyle: horizontalBarItemStyle(chart, point),
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
    if (chart.id === 'organization-department-growth-rate') {
      return buildDepartmentGrowthBumpOption(chart);
    }

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

  function buildDepartmentGrowthBumpOption(chart) {
    const series = normalizeSeries(chart);
    const labels = uniqueLabels(series);
    const rankData = buildDepartmentGrowthRankData(series, labels);
    const lastLabel = [...labels].reverse().find((label) => rankData.averageByLabel.has(label)) || labels[labels.length - 1];
    const lastAverage = rankData.averageByLabel.get(lastLabel) || 0;
    const lastAverageDisplay = formatSignedPercent(lastAverage);
    const rankCount = Math.max(series.length, 1);

    return {
      ...buildBaseOption(chart),
      color: [
        '#009E7A',
        '#2563EB',
        '#F59E0B',
        '#8B5CF6',
        '#EF4444',
        '#14B8A6',
      ],
      tooltip: {
        trigger: 'item',
        confine: true,
        backgroundColor: '#0F172A',
        borderWidth: 0,
        textStyle: {
          color: '#FFFFFF',
          fontSize: 12,
        },
        formatter: (params) => {
          const data = params.data || {};

          return [
            escapeHtml(data.periodLabel || params.name),
            escapeHtml(data.departmentName || params.seriesName),
            `순위: ${escapeHtml(formatRank(data.rank))}`,
            `성장률: ${escapeHtml(data.growthDisplayValue)}`,
            `전사 평균: ${escapeHtml(data.averageDisplayValue)}`,
            escapeHtml(data.averageStatusLabel),
          ].join('<br>');
        },
      },
      graphic: {
        type: 'text',
        right: 8,
        top: 0,
        style: {
          text: `전사 평균 성장률: ${lastAverageDisplay}`,
          fill: '#475569',
          font: '600 12px Pretendard, Noto Sans KR, sans-serif',
        },
      },
      grid: {
        top: 64,
        right: 190,
        bottom: 32,
        left: 50,
        containLabel: true,
      },
      xAxis: {
        type: 'category',
        data: labels,
        boundaryGap: false,
        axisTick: { show: false },
        axisLine: { lineStyle: { color: '#CBD5E1' } },
        axisLabel: {
          color: '#475569',
          fontSize: 12,
        },
      },
      yAxis: {
        type: 'value',
        inverse: true,
        min: 1,
        max: rankCount,
        interval: 1,
        splitLine: { lineStyle: { color: '#E2E8F0' } },
        axisLabel: {
          color: '#475569',
          fontSize: 12,
          formatter: (value) => formatRank(value),
        },
      },
      series: series.map((item) => ({
        name: item.name,
        type: 'line',
        data: labels.map((label) => rankData.dataByDepartmentAndLabel.get(item.name)?.get(label) || null),
        smooth: true,
        connectNulls: false,
        symbol: 'circle',
        symbolSize: 5,
        lineStyle: {
          width: 2,
        },
        label: {
          show: true,
          position: 'right',
          distance: 10,
          color: '#334155',
          fontSize: 11,
          fontWeight: 700,
          formatter: (params) => {
            const data = params.data || {};
            return data.isLastPoint
              ? `${data.departmentName} ${data.growthDisplayValue} ${data.averageStatus}`
              : '';
          },
        },
        labelLayout: {
          hideOverlap: true,
          moveOverlap: 'shiftY',
        },
        emphasis: {
          focus: 'series',
        },
      })),
    };
  }

  function buildDepartmentGrowthRankData(series, labels) {
    const orderByDepartment = new Map(series.map((item, index) => [item.name, index]));
    const pointByDepartmentAndLabel = new Map();
    const dataByDepartmentAndLabel = new Map();
    const averageByLabel = new Map();
    const finalLabel = labels[labels.length - 1];

    series.forEach((item) => {
      const pointsByLabel = new Map();

      normalizePoints(item).forEach((point) => {
        pointsByLabel.set(point.label, point);
      });

      pointByDepartmentAndLabel.set(item.name, pointsByLabel);
      dataByDepartmentAndLabel.set(item.name, new Map());
    });

    labels.forEach((label) => {
      const monthlyPoints = series
        .map((item) => {
          const point = pointByDepartmentAndLabel.get(item.name)?.get(label);

          return point ? {
            departmentName: item.name,
            value: numericValue(point.value),
            displayValue: point.displayValue || formatSignedPercent(numericValue(point.value)),
          } : null;
        })
        .filter(Boolean);

      if (!monthlyPoints.length) {
        return;
      }

      const average = monthlyPoints.reduce((sum, point) => sum + point.value, 0) / monthlyPoints.length;
      const averageDisplayValue = formatSignedPercent(average);
      averageByLabel.set(label, average);

      monthlyPoints
        .sort((a, b) => {
          if (b.value !== a.value) {
            return b.value - a.value;
          }

          const orderDiff = orderByDepartment.get(a.departmentName) - orderByDepartment.get(b.departmentName);
          return orderDiff !== 0 ? orderDiff : a.departmentName.localeCompare(b.departmentName, 'ko');
        })
        .forEach((point, index) => {
          const rank = index + 1;
          const isAboveAverage = point.value >= average;

          dataByDepartmentAndLabel.get(point.departmentName).set(label, {
            value: rank,
            rank,
            periodLabel: label,
            departmentName: point.departmentName,
            growthRate: point.value,
            growthDisplayValue: point.displayValue,
            averageGrowthRate: average,
            averageDisplayValue,
            averageStatus: isAboveAverage ? '▲ 평균 이상' : '▼ 평균 이하',
            averageStatusLabel: isAboveAverage ? '평균 이상' : '평균 이하',
            isLastPoint: label === finalLabel,
          });
        });
    });

    return {
      averageByLabel,
      dataByDepartmentAndLabel,
    };
  }

  function horizontalBarItemStyle(chart, point) {
    if (chart.id !== 'learning-kcy-participation') {
      return undefined;
    }

    return {
      color: point.label === '기타' ? '#E2E8F0' : '#009E7A',
    };
  }

  function buildGrowthFactorsOption(chart) {
    const series = normalizeSeries(chart);
    const growthSeries = series[0] || { name: '역량 증가폭', points: [] };
    const completedSeries = series[1] || { name: '수료 강의 수', points: [] };
    const labels = extractLabels(series);
    const growthPoints = normalizePoints(growthSeries);
    const completedPoints = normalizePoints(completedSeries);

    return {
      ...buildBaseOption(chart),
      color: ['#009E7A', '#2563EB'],
      tooltip: {
        trigger: 'axis',
        confine: true,
        backgroundColor: '#0F172A',
        borderWidth: 0,
        textStyle: {
          color: '#FFFFFF',
          fontSize: 12,
        },
        axisPointer: {
          type: 'shadow',
        },
      },
      legend: {
        bottom: 0,
        left: 'center',
        data: [growthSeries.name, completedSeries.name],
        itemWidth: 14,
        itemHeight: 10,
        textStyle: {
          color: '#475569',
          fontSize: 12,
        },
      },
      grid: {
        top: 30,
        right: 56,
        bottom: 48,
        left: 54,
        containLabel: true,
      },
      xAxis: {
        type: 'category',
        data: labels,
        axisTick: { show: false },
        axisLine: { lineStyle: { color: '#CBD5E1' } },
        axisLabel: {
          color: '#475569',
          fontSize: 12,
        },
      },
      yAxis: [
        {
          type: 'value',
          name: '역량 증가폭(%p)',
          nameTextStyle: {
            color: '#009E7A',
            fontSize: 12,
            fontWeight: 700,
            align: 'left',
          },
          splitLine: { lineStyle: { color: '#E2E8F0' } },
          axisLabel: {
            color: '#009E7A',
            fontSize: 12,
            formatter: (value) => `${value}`,
          },
        },
        {
          type: 'value',
          name: '수료 강의 수(건)',
          nameTextStyle: {
            color: '#2563EB',
            fontSize: 12,
            fontWeight: 700,
            align: 'right',
          },
          splitLine: { show: false },
          axisLabel: {
            color: '#2563EB',
            fontSize: 12,
            formatter: (value) => `${value}`,
          },
        },
      ],
      series: [
        {
          name: growthSeries.name,
          type: 'bar',
          yAxisIndex: 0,
          data: growthPoints.map((point) => ({
            value: numericValue(point.value),
            displayValue: point.displayValue,
          })),
          barWidth: 40,
          label: {
            show: true,
            position: 'top',
            color: '#0F766E',
            fontSize: 12,
            fontWeight: 700,
            formatter: (params) => params.data.displayValue || `${params.value}%p`,
          },
          itemStyle: {
            color: '#009E7A',
            borderRadius: [3, 3, 0, 0],
          },
        },
        {
          name: completedSeries.name,
          type: 'line',
          yAxisIndex: 1,
          data: completedPoints.map((point) => ({
            value: numericValue(point.value),
            displayValue: point.displayValue,
          })),
          smooth: false,
          symbol: 'circle',
          symbolSize: 9,
          lineStyle: {
            color: '#2563EB',
            width: 2,
          },
          itemStyle: {
            color: '#2563EB',
            borderColor: '#FFFFFF',
            borderWidth: 2,
          },
          label: {
            show: true,
            position: 'right',
            color: '#2563EB',
            fontSize: 12,
            fontWeight: 700,
            formatter: (params) => params.data.displayValue || `${params.value}건`,
          },
        },
      ],
    };
  }

  function buildRadarOption(chart) {
    const series = normalizeSeries(chart);
    const firstSeries = series[0] || { name: '', points: [] };
    const points = normalizePoints(firstSeries);
    const isLearningAverageSkill = chart.id === 'learning-average-skill';
    const useRightLegend = chart.id === 'organization-department-average-competency';
    const useLearningComparisonLegend = isLearningAverageSkill && series.length > 1;
    const hideLegend = isLearningAverageSkill && series.length < 2;
    const legendNames = series.map((item, index) => radarSeriesName(chart, item, index));
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
        radius: useRightLegend ? '60%' : useLearningComparisonLegend ? '58%' : '68%',
        center: useRightLegend ? ['38%', '54%'] : useLearningComparisonLegend ? ['50%', '60%'] : ['50%', '54%'],
        splitNumber: 4,
        axisName: {
          color: '#334155',
          fontSize: 12,
          lineHeight: 16,
        },
        axisNameGap: useRightLegend || useLearningComparisonLegend ? 20 : 15,
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
        data: legendNames,
        top: useLearningComparisonLegend ? 0 : undefined,
        left: useLearningComparisonLegend ? 'center' : undefined,
        bottom: useRightLegend ? 'middle' : useLearningComparisonLegend ? undefined : 0,
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
        data: series.map((item, index) => {
          const isCompanyAverage = chart.id === 'learning-average-skill' && index > 0;

          return {
            value: normalizePoints(item).map((point) => numericValue(point.value)),
            name: radarSeriesName(chart, item, index),
            areaStyle: {
              color: isCompanyAverage ? 'rgba(148, 163, 184, 0.12)' : undefined,
              opacity: isCompanyAverage ? 0.08 : 0.12,
            },
            lineStyle: {
              color: isCompanyAverage ? '#CBD5E1' : undefined,
              type: isCompanyAverage ? 'dashed' : 'solid',
              width: 2,
            },
            itemStyle: {
              color: isCompanyAverage ? '#CBD5E1' : undefined,
            },
          };
        }),
        lineStyle: {
          width: 2,
        },
      }],
    };
  }

  function radarSeriesName(chart, series, index) {
    if (chart.id !== 'learning-average-skill') {
      return series.name;
    }

    return index > 0 ? '전사 평균' : '선택 그룹';
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

  function buildHeatmapOption(chart) {
    const series = normalizeSeries(chart);
    const departments = series.map((item) => item.name);
    const competencies = uniqueLabels(series);
    const useTopCompetencyLabels = chart.id === 'organization-department-average-competency';
    const values = series.flatMap((item) => normalizePoints(item).map((point) => numericValue(point.value)));
    const minValue = values.length ? Math.min(...values) : 0;
    const maxValue = values.length ? Math.max(...values) : 100;
    const valueByDepartmentAndCompetency = new Map();

    series.forEach((item) => {
      const pointsByCompetency = new Map();
      normalizePoints(item).forEach((point) => {
        pointsByCompetency.set(point.label, point);
      });
      valueByDepartmentAndCompetency.set(item.name, pointsByCompetency);
    });

    const heatmapData = departments.flatMap((department, departmentIndex) => (
      competencies.map((competency, competencyIndex) => {
        const point = valueByDepartmentAndCompetency.get(department)?.get(competency);
        const value = point ? numericValue(point.value) : 0;

        return {
          value: [competencyIndex, departmentIndex, value],
          displayValue: point?.displayValue || formatValue(value, chart.unit),
          department,
          competency,
        };
      })
    ));

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
        formatter: (params) => {
          const data = params.data || {};
          return `${escapeHtml(data.department)}<br>${escapeHtml(data.competency)}: ${escapeHtml(data.displayValue)}`;
        },
      },
      grid: {
        top: useTopCompetencyLabels ? 52 : 22,
        right: 28,
        bottom: useTopCompetencyLabels ? 22 : 30,
        left: 112,
        containLabel: true,
      },
      xAxis: {
        type: 'category',
        position: useTopCompetencyLabels ? 'top' : 'bottom',
        data: competencies,
        axisTick: { show: false },
        axisLine: { lineStyle: { color: '#CBD5E1' } },
        axisLabel: {
          color: '#475569',
          fontSize: 12,
          interval: 0,
        },
      },
      yAxis: {
        type: 'category',
        data: departments,
        inverse: true,
        axisTick: { show: false },
        axisLine: { lineStyle: { color: '#CBD5E1' } },
        axisLabel: {
          color: '#475569',
          fontSize: 12,
          width: 100,
          overflow: 'truncate',
        },
      },
      visualMap: {
        show: false,
        min: minValue,
        max: maxValue,
        inRange: {
          color: ['#E8F7F2', '#9BE3CC', '#009E7A'],
        },
      },
      series: [{
        name: chart.title,
        type: 'heatmap',
        data: heatmapData,
        label: {
          show: true,
          color: '#0F172A',
          fontSize: 12,
          fontWeight: 700,
          formatter: (params) => params.data.displayValue,
        },
        itemStyle: {
          borderColor: '#FFFFFF',
          borderWidth: 2,
          borderRadius: 4,
        },
        emphasis: {
          itemStyle: {
            borderColor: '#009E7A',
            borderWidth: 2,
          },
        },
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

  function uniqueLabels(series) {
    const labels = [];
    const seen = new Set();

    series.forEach((item) => {
      normalizePoints(item).forEach((point) => {
        if (!seen.has(point.label)) {
          seen.add(point.label);
          labels.push(point.label);
        }
      });
    });

    return labels;
  }

  function numericValue(value) {
    const number = Number(value);
    return Number.isFinite(number) ? number : 0;
  }

  function formatValue(value, unit) {
    return `${Number(value).toLocaleString()}${unit || ''}`;
  }

  function formatSignedPercent(value) {
    const number = numericValue(value);
    const sign = number > 0 ? '+' : '';

    return `${sign}${number.toFixed(1)}%`;
  }

  function formatRank(value) {
    return `${Math.round(numericValue(value))}위`;
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

  function escapeHtml(value) {
    return String(value ?? '')
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#039;');
  }
})();
