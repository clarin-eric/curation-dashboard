$(document).ready( function () {
    $.extend( $.fn.dataTable.defaults, {
        paging: false,
        searching: false,
        scrollY: '70vh',
        sScrollX: "100%",
        scrollCollapse: true,
    } );

    $('#collections').DataTable();
    $('#profiles').DataTable();

} );


Dropzone.options.cmdiDropzone = {
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
        $('#cmdi-dropzone').append('<div>Upload complete. Curating...</div><div id="uploadWheel" class="spinner"></div>')
    }
  }
};

function toggleFacets() {
    var facetTable = $('#facetTable');

    if(facetTable.attr("hidden")){
        facetTable.removeAttr("hidden");
    }else{
        facetTable.attr("hidden",true);
    }
}


