$( document ).ready(function() {

jQuery('#vmap').vectorMap(
{
    map: 'world_en',
    backgroundColor: '#a5bfdd',
    borderColor: '#818181',
    borderOpacity: 0.25,
    borderWidth: 1,
    color: '#f4f3f0',
    //colors: map med land-kod (underscore) och hexkodad färg
    enableZoom: true,
    hoverColor: '#c9dfaf',
    hoverOpacity: null,
    normalizeFunction: 'linear',
    scaleColors: ['#b6d6ff', '#005ace'],
    selectedColor: '#c9dfaf',
    selectedRegions: [],
    showTooltip: true,
    onRegionClick: function(element, code, region)
    {
        var message = 'You clicked "'
            + region
            + '" which has the code: '
            + code.toUpperCase();

        alert(message);
        //show info
        //add movie button
    },
    onLoad: function(event, map)
    {
        //load data
    },
    onRegionOver: function(event, code, region)
    {
        //show info
    },
});

});
