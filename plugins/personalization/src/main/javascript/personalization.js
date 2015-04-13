$(function() {
  var banners = $("div.personalization-banner");
  
  var fallback = function() {
      banners.append('<div class="description">Standard non-personalized ad</div>');
  };

  if (banners.size() >= 1 && document.cookie.indexOf("accessToken") >= 0) {
    var userId = $.cookie("accessToken").split('::')[0];
    $.ajax({ url: "/personalization?userId="+userId, dataType: "json"}).done(function(data) {
      if (typeof(data) === "object") {
        banners.append('<div class="description">Personalized ad keywords:</div>');

        var tags = [];
        Object.keys(data).forEach(function(tag) {
          tags.push({ tag: tag, weight: data[tag] });
        });
        tags.sort(function(a, b) {
          return b.weight - a.weight;
        });

        var max = Math.min(tags.length, 10);
        tags = tags.slice(0, max);
        var total = tags.reduce(function(prev, curr) {
          return prev + curr.weight;
        }, 0);

        tags.sort(function(a, b) {
            return Math.random() - 0.5;
        });
        
        for (var i = 0; i < max; i++) {
          var tag = tags[i].tag.replace(/\W+/g, '-');
          var weight = tags[i].weight / total;
          banners.append('<span class="tag" style="font-size: calc(' + Math.round(100 + Math.log(weight*10) * 25) + '%)">' + tag + '</span> ');
        }
      }
    }).fail(fallback);
  } else {
    fallback();
  }
});