const advancedSearch = {
    removeKeyword: function(index) {
        const tbody = document.getElementById("advanced_search_keywords-list");
        const row = document.getElementById(`advanced_search_keywords-list-row-${index}`);
        const reply_click = function(){
            tbody.removeChild(row);
            document.getElementById("advanced_search-form").submit();
        }
        window.addEventListener("DOMContentLoaded", (event) => {
            const element = document.getElementById(`advanced_search_keywords-list-row-${index}-remove_button`)
              element.addEventListener('click', reply_click, false);
        });
    }
};

const checkboxes = {
    filterCases: function() {
      const replyChange = function(){
        document.getElementById("advanced_search-form").submit();
      }
      window.addEventListener("DOMContentLoaded", (event) => {
          document.querySelectorAll(
          "#application_type-0, #application_type-1, #application_type-2, #application_type-3, #status-0, " +
          "#status-1, #status-2, #status-3, #status-4, #status-5, #status-6, #status-7, #status-8"
          )
          .forEach(elem => elem.addEventListener("change", replyChange));
      });
    }
}

