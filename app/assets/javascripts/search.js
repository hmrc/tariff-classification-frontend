var advancedSearch = {
    removeKeyword: function(index) {
        var tbody = document.getElementById("advanced_search_keywords-table-table_body");
        var row = document.getElementById("advanced_search_keywords-table-row-" + index);
        tbody.removeChild(row);
        document.getElementById("advanced_search-form").submit();
    }
};