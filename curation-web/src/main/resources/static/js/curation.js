$( document ).ready(function() {
    if(location.pathname == '/'){
        $(".nav-link").first().addClass("active");
        return;
    }
    $(".nav-link").each(function (){
        if($(this).attr("href") == location.pathname){
            $(this).addClass("active");
            return;
        }
    });
});
new DataTable('.dataTable', {
    columnControl: {
        target: 1, // adding the search row as row 1 with row in {0, 1,...}
        content: ['search'] //adding a search row
    },
    info: false,
    paging: false,
    searching: true, // don't disable, since the column search is depending on it!
    scrollY: '70vh',
    scrollX: true,
    scrollCollapse: true,
    order: [], // the data is ordered already
    orderCellsTop: true,
    fixedHeader: true,
    layout: {
        topEnd: null //top prevent a general search field
    }
});

Dropzone.autoDiscover = false;

$("button#cmdi-dropzone").dropzone({
    url: "/validator",
    headers: {'Accept': 'text/html, application/xhtml+xml'},
    paramName: "file", // The name that will be used to transfer the file
      maxFilesize: 5, // MB
      success: function(file, done) {
            document.open();
            document.write(done);
            document.close();
       },
       error: function(file, error){
            document.open();
            document.write(error);
            document.close();
      },
      totaluploadprogress: function(progress, bytesSent) {
        //disable validate button first.
        $('#validateButton').prop('disabled', true);

        if(progress == 100){
            $('button#cmdi-dropzone').append('<div>Upload complete. Validating...&nbsp;&nbsp;&nbsp;&nbsp;</div><div class="spinner-border" role="status"><span class="visually-hidden">Validating...</span></div>');
        }
      }
});

$('#validateButton').click(function() {

    //only change to spinner if input is valid
    if($("#url-input").val()){
        $("#validatorTabPanel").attr("hidden", true);
        $("#validatorSpinner").attr("hidden", false);
    }
});

//add toggle logic to all buttons on the table and remove the class from them so that the logic doesn't get applied twice on ajax call
$(".showUrlInfo").click(function() {

    if($(this).parent().parent().next().attr("hidden")){
        $(this).parent().parent().next().removeAttr("hidden");
        $(this).text("Hide");
    }
    else{
        $(this).parent().parent().next().attr("hidden", true);
        $(this).text("Show")
    }
});
$(".showUrlInfo").removeClass("showUrlInfo");
/*
$(".nav-item").on("click", function (){
    $("li.active").removeClass("active");
    $(this).addClass("active");
});

 */








