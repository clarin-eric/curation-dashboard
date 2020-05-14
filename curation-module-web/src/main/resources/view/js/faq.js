$(document).ready( function () {
    $.ajax({url: "/faq/faq.md", success: function(result){
        //convert md to html with showdownjs and return
        var converter = new showdown.Converter();
        var html = converter.makeHtml(result);

        $("#faq").html(html);
      }});
});