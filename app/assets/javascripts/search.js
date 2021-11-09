var advancedSearch = {
    removeKeyword: function(index) {
        var tbody = document.getElementById("advanced_search_keywords-list");
        var row = document.getElementById("advanced_search_keywords-list-row-" + index);
        tbody.removeChild(row);
        document.getElementById("advanced_search-form").submit();
    }
};
