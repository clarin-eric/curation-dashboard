$(document).ready( function () {
    $.extend( $.fn.dataTable.defaults, {
        paging: false,
        searching: false,
        scrollY: '70vh',
        sScrollX: "100%",
        scrollCollapse: true,
    } );

    $('#collections').DataTable();

} );


Dropzone.options.cmdiDropzone = {
  paramName: "file", // The name that will be used to transfer the file
  maxFilesize: 5, // MB
  success: function(file, done) {
    window.location.href="/instance/"+file.name.split(".")[0] + ".html";
  },
  error: function(file, error){
    document.open();
    document.write(error);
    document.close();
  }
};