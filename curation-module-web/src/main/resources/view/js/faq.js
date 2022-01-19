$(document).ready( function () {

    $.ajax({url: "https://raw.githubusercontent.com/clarin-eric/clarin-curation-module/master/curation-module-web/src/main/resources/view/markdown/faq.md",
        success: function(result){
            $("#faq").html(convert(result));
        },
        error: function(XMLHttpRequest, textStatus, errorThrown){
            //if github link doesnt work, get it from resources (this way, github can be updated without redeploying)
            $.ajax({url: "/faq/faq.md", success: function(result){
                $("#faq").html(convert(result));
            }});
        }
    });
});

//convert md to html with showdownjs and return
function convert(mdFile){
        var converter = new showdown.Converter({tables: true});
        return converter.makeHtml(mdFile);
}