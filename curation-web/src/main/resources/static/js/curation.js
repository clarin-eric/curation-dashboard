$(document).ready( function () {
    $.extend( $.fn.dataTable.defaults, {
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
    } );
    // duplicating the header line
    $('.dataTable thead tr').clone(true).appendTo('.dataTable thead');
    // replacing the text in the second header line with input boxes for column search
    $('.dataTable').DataTable().columns()
        .every(function () {
            let column = this;
            let title = column.header(1).textContent;

            // Create input element
            let input = document.createElement('input');
            input.placeholder = 'search ' + title;
            column.header(1).replaceChildren(input);

            // Event listener for user input
            input.addEventListener('keyup', () => {
                if (column.search() !== this.value) {
                    column.search(input.value).draw();
                }
            });
        });
} );

Dropzone.autoDiscover = false;

$("div#cmdi-dropzone").dropzone({
    url: "/curate",
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
            $('div#cmdi-dropzone').append('<div>Upload complete. Validating...&nbsp;&nbsp;&nbsp;&nbsp;</div><div class="spinner-border" role="status"><span class="visually-hidden">Validating...</span></div>');
        }
      }
});

$('#validateButton').click(function() {

    //only change to spinner if input is valid
    if($('#url-input').val()){
        $(this).parent().parent().attr("hidden", true);
        $(this).parent().parent().next().attr("hidden", false);
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








