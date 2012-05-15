PM.show_questions = function(questions, questionlist, on_submit) {
    var grouped_questions = _.groupBy(questions, function(q) { return q.category_name; });
    _.each(grouped_questions, function(quests, category) {
               questionlist.append('<h2>{0}</h2>'.format(category));
               _.map(quests, function(q) { 
                         PM.show_question(q, questionlist); 
                         $('#q' + q.id + ' .plus').click(_.bind(PM.add_fields, PM, q));
                     }); 
           });

    var button_id = UTILS.gen_id();
    questionlist.append('<input type="button" value="submit" id="submit{0}"/>'.format(button_id));
    questionlist.append('<hr/>');
    $('#submit' + button_id).click(on_submit);
};

PM.add_fields = function(question) {
    console.log('+PLUS+');
    console.log(question);
    var question_html = PM.get_question_html(question);
    $('#q' + question.id + ' .plus').last().remove();
    $('#q' + question.id).append('<br>' + question_html);
    $('#q' + question.id + ' .plus').click(_.bind(PM.add_fields, PM, question));

    return false;
};

PM.get_question_widget = function(question, index) {
    // by convention the id of the input for the question N is iqN
    var widget_to_html = {
        text: function(id, proposed_answers, formal_answers) {
            return '<input class="inputfield" type="text" id="iq{0}" />'.format(id);
        },
        radio: function(id, proposed_answers, formal_answers) {
            var html = "";
            _.each(formal_answers, function(formal_answer, index) {
                       html += '<input id="iq{0}" class="radiobutton inputfield" name="inputq{0}" value="{1}" type="radio" />{2} '
                           .format(id, formal_answer, proposed_answers[index]);
                   });
            return html;
        },
        select: function(id, proposed_answers, formal_answers) {
            var html = '<select class="combobox">';
            
            _.each(formal_answers, function(formal_answer, index) {
                       html += '<option id="iq{0}" class="dropdown-menu inputfield" value="{1}">{2}</option>'
                           .format(id, formal_answer, proposed_answers[index]);
                   });
            html += '</select>';
            return html;
        }
    };

    return widget_to_html[question.widgets[index]](question.id, question.answers, question.formalanswers);
};

PM.show_question = function(question, questionlist) {
    questionlist.append('<p>{0}</p>'.format(question.hint == null ? "" : question.hint));

    var question_html = PM.get_question_html(question);
    questionlist.append('<div id="q{0}">{1}</div>'.format(question.id, question_html));
    questionlist.append('<br/>');
};

PM.get_question_html = function(question) {
    var variable = /\?[a-zA-Z_0-9-]+/;

    var html = "";
    if(question.yesnoquestion) {
        html = question.question + PM.get_question_widget(question, 0);
    } else {
        html = question.question.replace(variable,
                                         function(match, index) {
                                             console.log('get_question_html ' + index);
                                             return PM.get_question_widget(question, index);
                                         });
        
    }
    
    html = '<div class="question">{1}&nbsp;&nbsp;<img style="vertical-align: middle;" width="18" height="18" class="minus" src="images/minus.png">&nbsp;&nbsp;</img><img style="vertical-align: middle;" width="18" height="18" class="plus" src="images/plus.png"></img></div>'.format(question.id, html);
    
    return html;
};