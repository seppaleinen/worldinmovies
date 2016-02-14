'use strict';

$(document).ready(function() {
    var default_color = "#8B0000";
    var found_color = "#00ff00";

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

    function renderMap() {
        jQuery('#vmap').vectorMap({
            map: 'world_en',
            hoverOpacity: 0.7,
            hoverColor: false,
            onRegionClick: function(element, code, region) {
                var message = 'You clicked "' + region + '" which has the code: ' + code.toUpperCase();

                jQuery('#popup').text(message);
                if ($(this).hasClass('selected')) {
                    deselect($(this));
                } else {
                    $(this).addClass('selected');
                    $('.pop').slideFadeToggle();
                }

                $('.close').on('click', function() {
                    deselect($('#contact'));
                    return false;
                });
                //show info
                //add movie button
            },
            onLoad: function(event, map) {
                //load data
                var data = jQuery('#data').text();

                if ((data.length === 0 || !data.trim())) {
                    $.ajax({
                        url: '/findCountries',
                        type: 'GET',
                        crossDomain: false,
                        //data: 'ID=1&Name=John&Age=10', // or $('#myform').serializeArray()
                        success: function(data) {
                            var colors = {};
                            for (var i in data) {
                                colors[data[i].code] = default_color;
                                var country = map.countries[data[i].code.toLowerCase()];
                                if (country != undefined) {
                                    map.countries[data[i].code.toLowerCase()].setFill(default_color);
                                }
                            }
                            console.log(Object.keys(colors).length);
                        },
                        error: function(jqXHR, textStatus, errorThrown) {
                            var message = 'Call to backend failed';

                            jQuery('#popup').text(message);
                            $(this).addClass('selected');
                            $('.pop').slideFadeToggle();

                            $('.close').on('click', function() {
                                deselect($('#contact'));
                                return false;
                            });
                        }
                    });
                } else {
                    var colors = {};
                    data = jQuery.parseJSON(data);
                    $.each(jQuery.parseJSON(data), function(key, value) {
                        //console.log(value.name);
                        colors[jQuery.parseJSON(data)[key].country] = default_color;
                        if(jQuery.parseJSON(data)[key].country) {
                            var country = map.countries[jQuery.parseJSON(data)[key].country.toLowerCase()];
                            if (country != undefined) {
                                map.countries[jQuery.parseJSON(data)[key].country.toLowerCase()].setFill(found_color);
                            }
                        }
                    });
                }
            },
            onRegionOver: function(event, code, region) {
                //show info
            },
            onLabelShow: function(event, label, code) {
                //show info
                //label.text = "HEJ";
            },
        });
    }

    renderMap();

});