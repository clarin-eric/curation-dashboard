$(document).ready( function () {
    $.extend( $.fn.dataTable.defaults, {
        paging: false,
        searching: false,
        scrollY: '70vh',
        sScrollX: "100%",
        scrollCollapse: true
    } );

    var arr = [];
    for(var i = 1;i<26;i++){
        arr.push(i);
    }

//right align number columns (shitty code to make it manual but i dont know how to do it dynamicly)
    $('#collections').DataTable({
        columnDefs: [
            { className: 'text-right', targets: arr }
          ]
    });
    $('#profiles').DataTable({
        columnDefs: [
            { className: 'text-right', targets: [2, 3, 18] }
          ]
    });

} );

Dropzone.autoDiscover = false;

$("div#cmdi-dropzone").dropzone({
    url: "/curate",
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
        if(progress == 100){
            $('div#cmdi-dropzone').append('<div>Upload complete. Curating...</div><div id="uploadWheel" class="spinner"></div>')
        }
      }
});


//Dropzone.options.cmdiDropzone = {
//  paramName: "file", // The name that will be used to transfer the file
//  maxFilesize: 5, // MB
//  success: function(file, done) {
//        document.open();
//        document.write(done);
//        document.close();
//   },
//   error: function(file, error){
//        document.open();
//        document.write(error);
//        document.close();
//  },
//  totaluploadprogress: function(progress, bytesSent) {
//    if(progress == 100){
//        $('#cmdi-dropzone').append('<div>Upload complete. Curating...</div><div id="uploadWheel" class="spinner"></div>')
//    }
//  }
//};

function toggleFacets() {
    var facetTable = $('#facetTable');

    if(facetTable.attr("hidden")){
        facetTable.removeAttr("hidden");
    }else{
        facetTable.attr("hidden",true);
    }
}



$('#validateButton').click(function() {
    //only change to spinner if input is valid
    if($('#url-input').val()){
        $(this).html('<div>Curating...</div><div id="uploadWheel" class="spinner"></div>')
        $(this).prop('disabled', true);
    }
});


var table = $('#statsTable');
var collectionName = table.attr("data-collection");
var statusCode = table.attr("data-status");
var reportTableTbody = $('#reportTableTbody');

var i = 1;
var scrollReady = true;

//if end of table is visible, add 100 more elements to it
$(window).scroll(function() {
    if(scrollReady){

        //stackoverflow copied
        var tableEnd = $("#tableEndSpan");

        if(tableEnd.length){
            var top_of_element = $("#tableEndSpan").offset().top;
            var bottom_of_element = $("#tableEndSpan").offset().top + $("#tableEndSpan").outerHeight();
            var bottom_of_screen = $(window).scrollTop() + $(window).innerHeight();
            var top_of_screen = $(window).scrollTop();

            if ((bottom_of_screen > top_of_element) && (top_of_screen < bottom_of_element)){

                scrollReady=false;

                tableEnd.append('<div id="uploadWheel" class="spinner"></div>')
                setTimeout(function(){
                    $("#uploadWheel").remove();

                    // ajax call get data from server and append to the table
                    $.ajax({
                       dataType: "html",
                       url: "/statistics/"+collectionName+"/"+statusCode+"/"+i,
                       success: function (data) {
                           reportTableTbody.append(data);

                           //this assures that if ever an empty element returns, there are no more requests sent
                           if(data){
                               i++;
                               scrollReady=true;
                           }
                       }
                    });



                }, 1000);//wait a second between scroll and call




            } else {
                //do nothing
            }
        }
    }

});






