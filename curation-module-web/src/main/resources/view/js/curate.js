$(document).ready( function () {
    $.extend( $.fn.dataTable.defaults, {
        paging: false,
        searching: true,
        scrollY: '70vh',
        sScrollX: "100%",
        scrollCollapse: true
    } );
    // Setup - add a text input to each footer cell
    $('#collections thead tr').clone(true).appendTo( '#collections thead' );
    $('#collections thead tr:eq(1) th').each( function (i) {
        var title = $(this).text();
        $(this).html( '<input type="text" placeholder="Search '+title+'" />' );
 
        $( 'input', this ).on( 'keyup change', function () {
            if ( table.column(i).search() !== this.value ) {
                table
                    .column(i)
                    .search( this.value )
                    .draw();
            }
        } );
    } );
 
    var table = $('#collections').DataTable( {

        orderCellsTop: true,
        fixedHeader: true,
        
        columnDefs: [
    		{targets: 0,	className: 'dt-body-left'},
    		{targets: '_all',  className: 'dt-body-right'}
    	]
    } );    
    $('#profiles thead tr').clone(true).appendTo( '#profiles thead' );
    $('#profiles thead tr:eq(1) th').each( function (i) {
        var title = $(this).text();
        $(this).html( '<input type="text" placeholder="Search '+title+'" />' );
 
        $( 'input', this ).on( 'keyup change', function () {
            if ( table.column(i).search() !== this.value ) {
                table
                    .column(i)
                    .search( this.value )
                    .draw();
            }
        } );
    } );
    

 
    var table2 = $('#profiles').DataTable( {

        orderCellsTop: true,
        fixedHeader: true,
        
        columnDefs: [
            {targets: [2, 3, 4, 5, 20],    className: 'dt-body-right'},
            {targets: '_all',  className: 'dt-body-left'}
        ],
        'rowCallback': function(row, data, index){
            for(var i=6; i<=19;i++){
                if(data[i] == 'true'){
                    $(row).find('td:eq(' + i + ')').css('background-color', 'lightgreen');
                }
                else{
                    $(row).find('td:eq(' + i + ')').css('background-color', 'lightcoral');
                }
            }
          }
    } );


    $("#collections_filter").hide();//hide top filter which is not necessary
    $("#profiles_filter").hide();//hide top filter which is not necessary

    redirectDeprecatedURLs();
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

                           //add toggle logic to all new buttons on the table and remove the class from them so that the logic doesn't get applied twice on ajax call
                           $(".showUrlInfo").click(function() {
                               toggleInfo($(this));
                           });
                           $(".showUrlInfo").removeClass("showUrlInfo");
                       }
                    });

                }, 1000);//wait a second between scroll and call

            } else {
                //do nothing
            }
        }
    }

});

//add toggle logic to all buttons on the table and remove the class from them so that the logic doesn't get applied twice on ajax call
$(".showUrlInfo").click(function() {
    toggleInfo($(this));
});
$(".showUrlInfo").removeClass("showUrlInfo");

function toggleInfo(button){

    var element = button.parent().parent().next();

    if(element.attr("hidden")){
        element.removeAttr("hidden");
        button.text("Hide");
    }else{
        element.attr("hidden",true);
        button.text("Show");
    }

}

//harvester uses old links that won't work anymore
//so for backwards compatibility's sake, we have a redirect for deprecated paths
//when menzo returns and updates the harvester, this method can be deleted
function redirectDeprecatedURLs(){

    if(window.location.hash.startsWith("#!ResultView/collection//")){

        var hashArray = window.location.hash.split("/");

        if(hashArray !== undefined && hashArray.length != 0){
            var collectionName = hashArray[hashArray.length-1];

            var newUrl = window.location.origin + "/collection/"+ collectionName+".html";

            window.location.replace(newUrl);
        }
    }




}








