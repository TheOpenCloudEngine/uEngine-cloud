module.exports = function (iam) {
  let module = {};

  module.iam = iam;

  module.requireGuest = function (to, from, next) {
    // will stop the routing if isAuthenticated
    module.isAuthenticated(function (result) {
      next(!result);
    })
  };

  module.requireUser = function (to, from, next) {
    //isAuthenticated ? continue route
    //if not ? will login and come back
    module.isAuthenticated(function (result) {
      if (result) {
        next(result);
      } else {
        next({
          path: '/auth/login',
          query: {
            redirect: to.fullPath
          }
        })
      }
    });
  };
  module.isAuthenticated = function (callback) {
    if (!localStorage['access_token']) {
      callback(false);
    }
    module.iam.validateToken(localStorage['access_token'])
      .done(function (info) {
        localStorage['user'] = JSON.stringify(info.context.user);
        localStorage['scopes'] = JSON.stringify(info.context.scopes);
        localStorage['userName'] = info.context['userName'];
        localStorage['acl'] = info.context.user['metaData'].acl;
        localStorage['gitlab-id'] = info.context.user['metaData']['gitlab-id'];
        callback(true);
      })
      .fail(function () {
        callback(false);
      });
  };
  return module;
};
