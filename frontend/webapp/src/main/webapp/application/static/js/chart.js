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
        var data = jQuery('#data').text();

        if (data.length === 0 || !data.trim()) {
            $('#imdbRatings').click();
        } else {
            var map = {};
            data = jQuery.parseJSON(data);
            $.each(jQuery.parseJSON(data), function(key, value) {
                if (value.country) {
                    var countryCode = value.country.toLowerCase();
                    var i = map[countryCode];
                    if (i != undefined && i.length > 0) {
                        console.log(i);
                        i.push(value.name);
                        map[countryCode] = i;
                    } else {
                        var array = [];
                        array.push(value.name);
                        map[countryCode] = array;
                    }
                }
            });
            var data = [];
            for (var key in map) {
                if (map.hasOwnProperty(key)) {
                    data.push(new ChartItem(map[key].length, "#F7464A", "#FF5A5E", key));
                    //alert(key + " -> " + p[key]);
                }
            }
            var ctx2 = document.getElementById("chart-area").getContext("2d");
            window.myPie = new Chart(ctx2).Pie(data);
        }
    }

    renderChart();
});