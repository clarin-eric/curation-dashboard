package eu.clarin.cmdi.curation.utils;

import eu.clarin.cmdi.rasa.helpers.statusCodeMapper.Category;

import java.util.EnumMap;

//this class assigns categories to colors
public final class CategoryColor {
    public static EnumMap<Category, String> categoryColor = new EnumMap<>(Category.class);

    static{

        categoryColor.put(Category.Ok,"#cbe7cc");
        categoryColor.put(Category.Broken,"#f2a6a6");
        categoryColor.put(Category.Undetermined,"#fff7b3");
        categoryColor.put(Category.Restricted_Access,"#6c7ae7");
        categoryColor.put(Category.Blocked_By_Robots_txt,"#b269e5");
    }

    public static String getColor(Category category){
        return categoryColor.get(category);
    }

}
