var advancedSearch = {
    removeKeyword: function(index) {
        var tbody = document.getElementById("advanced_search_keywords-list");
        var row = document.getElementById(`advanced_search_keywords-list-row-${index}`);
        var reply_click = function(){
            tbody.removeChild(row);
            document.getElementById("advanced_search-form").submit();
        }
        window.addEventListener("DOMContentLoaded", (event) => {
            const element = document.getElementById(`advanced_search_keywords-list-row-${index}-remove_button`)
              element.addEventListener('click', reply_click, false);
        });
    }
};
