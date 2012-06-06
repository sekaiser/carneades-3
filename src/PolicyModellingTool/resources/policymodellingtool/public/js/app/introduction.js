PM.display_introduction = function() {
    PM.ajax_post(IMPACT.simulation_url, {"current-policy": null},
                 function(currentpolicy) {
                     IMPACT.current_policy = currentpolicy; 
                 });

    var introduction_html = ich.introduction();
    $('#pm').html(introduction_html.filter("#introduction"));
    $('#start').click(PM.on_start_button);
    PM.activate('#introduction-item');
};

PM.on_start_button = function() {

    // reset session
    PM.ajax_post(IMPACT.simulation_url, {reset: null}, function() {});

    PM.set_issues_url();
    return false;  
};
