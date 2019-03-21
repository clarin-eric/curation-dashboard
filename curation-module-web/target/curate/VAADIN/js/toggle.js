function toggle(){				
	var lst = document.getElementsByClassName('noFacet');
    for(var i = 0; i < lst.length; ++i) {
        lst[i].style.display = lst[i].style.display === 'none' ? '' : 'none';
    }
    
    var button = document.getElementById("toggle");
    if (button.innerHTML=="Show Facet Values") button.innerHTML = "Show All Values";
    else button.innerHTML = "Show Facet Values";
}