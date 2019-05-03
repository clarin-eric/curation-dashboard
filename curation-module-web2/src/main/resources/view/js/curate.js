$(document).ready( function () {
    $.extend( $.fn.dataTable.defaults, {
        paging: false,
        searching: false,
        scrollY: '70vh',
        sScrollX: "100%",
        scrollCollapse: true,
    } );

    $('#collections').DataTable();

} );//todo make it all tables


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


