if(typeof String.prototype.startsWith != 'function') {
    String.prototype.startsWith = function(str) {
        return this.slice(0, str.length) == str
    }
}

if(typeof String.prototype.endsWith != 'function') {
    String.prototype.endsWith = function(str) {
        return this.slice(-str.length) == str
    }
}

var determineSearchRoute = function(searchParam) {
    if(searchParam.startsWith("profile:")) {
        return "/search/profiles/" + searchParam.substring("profile:".length)
    }
    else if(searchParam.startsWith("title:")) {
        return "/search/titles/" + searchParam.substring("profile:".length)
    }
    else if(searchParam.startsWith("paste:")) {
        return "/search/pastes/" + searchParam.substring("paste:".length)
    }

    return "/p/" + searchParam
}
$(document).ready(function() {
    $("#submitButton").click(function() {
        var searchBoxValue = $("#searchTerm").val()
        location.href = determineSearchRoute(searchBoxValue)
        return false
    })
})
