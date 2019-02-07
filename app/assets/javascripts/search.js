var advancedSearch = {

    addKeyword: function() {
        var keywordInput = document.getElementById("keyword_0");
        var value = keywordInput.value.toUpperCase();
        var selectedAlready = advancedSearch.isSelectedKeyword(value);

        if(selectedAlready) {
            keywordInput.value = "";
        } else if(value && value.length > 0) {
            var tbody = document.getElementById("advanced_search-keywords-table_body");
            var tr_index = tbody.childElementCount + 1;

            var td_keyword_input = document.createElement("input");
            td_keyword_input.setAttribute("type", "hidden");
            td_keyword_input.setAttribute("name", "keyword[" + tr_index + "]");
            td_keyword_input.setAttribute("id", "keyword_" + tr_index);
            td_keyword_input.setAttribute("value", value);

            var td_keyword_span = document.createElement("span");
            td_keyword_span.textContent = value;

            var td_keyword = document.createElement("td");
            td_keyword.appendChild(td_keyword_span);
            td_keyword.appendChild(td_keyword_input);

            var td_remove_button = document.createElement("button");
            td_remove_button.setAttribute("type", "button");
            td_remove_button.setAttribute("onclick", "advancedSearch.removeKeyword(" + tr_index + ")");
            td_remove_button.classList.add("button-link");
            td_remove_button.textContent = "Remove";

            var td_remove = document.createElement("td");
            td_remove.appendChild(td_remove_button);

            var tr = document.createElement("tr");
            tr.setAttribute("id", "advanced_search-keywords-row-" + tr_index);
            tr.appendChild(td_keyword);
            tr.appendChild(td_remove);

            tbody.appendChild(tr);

            keywordInput.value = "";
            document.getElementById("search_form").submit();
        }
    },
    removeKeyword: function(index) {
        var tbody = document.getElementById("advanced_search-keywords-table_body");
        var row = document.getElementById("advanced_search-keywords-row-" + index);
        tbody.removeChild(row);
        document.getElementById("search_form").submit();
    },
    isSelectedKeyword: function(value) {
        var tbody = document.getElementById("advanced_search-keywords-table_body");
        var inputs = tbody.getElementsByTagName("input");
        for(var i=0; i<inputs.length; i++) {
            if(inputs.item(i).value === value) {
                return true;
            }
        }
        return false;
    }
};