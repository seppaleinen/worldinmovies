'use strict';

function ChartItem(value, color, highlight, label) {
    this.value = value;
    this.color = color;
    this.highlight = highlight;
    this.label = label;
}

$(document).ready(function() {
    function deselect(e) {
        $('.pop').slideFadeToggle(function() {
            e.removeClass('selected');
        });
    }
    $.fn.slideFadeToggle = function(easing, callback) {
        return this.animate({
            opacity: 'toggle',
            height: 'toggle'
        }, 'fast', easing, callback);
    };

    function renderChart() {
        var map = {};
        var chartData = [];

        $.ajax({
            url: '/user_info',
            type: 'POST',
            crossDomain: false,
            success: function(data) {
                $.each(jQuery.parseJSON(data), function(key, value) {
                    if (value.countrySet) {
                        for (var i = 0, len = value.countrySet.length; i < len; i++) {
                            var countryCode = value.countrySet[i].toLowerCase();
                            var i = map[countryCode];
                            if (i != undefined && i.length > 0) {
                                i.push(value.name);
                                map[countryCode] = i;
                            } else {
                                var array = [];
                                array.push(value.name);
                                map[countryCode] = array;
                            }
                        }
                    }
                });
                for (var key in map) {
                    if (map.hasOwnProperty(key)) {
                        chartData.push(new ChartItem(map[key].length, "#F7464A", "#FF5A5E", key));
                    }
                }
                var ctx2 = document.getElementById("chart-area").getContext("2d");
                window.myPie = new Chart(ctx2).Pie(chartData, {
                    animationEasing: "easeOutBounce",
                    animateRotate: true,
                    animateScale: false,
                    customTooltips: function(tooltip) {
                        var tooltipEl = $('#chartjs-tooltip');
                        // Hide if no tooltip
                        if (!tooltip) {
                            tooltipEl.css({
                                opacity: 0
                            });
                            return;
                        }
                        // Set caret Position
                        tooltipEl.removeClass('above');
                        tooltipEl.addClass(tooltip.yAlign);
                        // Set Text
                        tooltipEl.html(tooltip.text + " movies");
                        // Find Y Location on page
                        var top = tooltip.y - tooltip.caretHeight - tooltip.caretPadding;;
                        // Display, position, and set styles for font
                        tooltipEl.css({
                            opacity: 1,
                            left: tooltip.chart.canvas.offsetLeft + tooltip.x + 'px',
                            top: tooltip.chart.canvas.offsetTop + top + 'px',
                            fontFamily: tooltip.fontFamily,
                            fontSize: tooltip.fontSize,
                            fontStyle: tooltip.fontStyle,
                        });
                    }
                });
            },
            error: function(jqXHR, textStatus, errorThrown) {
                var message = 'Call to backend failed';

                jQuery('#popup').text(message);
                $(this).addClass('selected');
                $('.pop').slideFadeToggle();
            }
        });


    }

    renderChart();
});