/**
 * Created by Seungpil Park on 2016. 9. 6..
 */
var IAM = function (host, contextPath) {
  this.host = host;
  this.contextPath = contextPath;
  this.clientKey = null;
  this.clientSecretKey = null;
  if (!host) {
    this.baseUrl = location.protocol + '//' + location.hostname + (location.port ? ':' + location.port : '');
  } else {
    this.baseUrl = this.host;
  }
  if (this.contextPath) {
    this.baseUrl = this.baseUrl + this.contextPath;
  }
  this.user = undefined;

  $(window).ajaxSend(function (e, xhr, options) {
    var token = localStorage.getItem('uengine-iam-access-token');
    var clientKey = localStorage.getItem('uengine-iam-client-key');
    var clientSecret = localStorage.getItem('uengine-iam-client-secret');
    xhr.setRequestHeader('Authorization', token);
    if (clientKey && clientSecret) {
      xhr.setRequestHeader('client-key', clientKey);
      xhr.setRequestHeader('client-secret', clientSecret);
    }
  });
};
IAM.prototype = {
  logout: function () {
    localStorage.removeItem('uengine-iam-access-token');
    // localStorage.removeItem('uengine-iam-client-key');
    // localStorage.removeItem('uengine-iam-client-secret');
  },
  setDefaultClient: function (key, secret) {
    localStorage.setItem('uengine-iam-client-key', key);
    localStorage.setItem('uengine-iam-client-secret', secret);
  },
  adminLogin: function (username, password) {
    if (username) {
      localStorage.setItem('uengine-iam-client-key', username);
    }
    if (password) {
      localStorage.setItem('uengine-iam-client-secret', password);
    }
    var options = {
      type: "GET",
      url: '/rest/v1/security',
      dataType: "json",
      async: false
    };
    return this.send(options);
  },
  system: function () {
    var options = {
      type: "GET",
      url: '/rest/v1/system',
      dataType: "json"
    };
    return this.send(options);
  },
  passwordCredentialsLogin: function (username, password, scope, token_type, claim) {
    var data = {
      username: username,
      password: password,
      scope: scope,
      token_type: token_type,
      claim: claim,
      client_id: localStorage.getItem('uengine-iam-client-key'),
      client_secret: localStorage.getItem('uengine-iam-client-secret'),
      grant_type: 'password'
    };
    for (var key in data) {
      if (!data[key]) {
        delete data[key];
      }
    }
    var query_string = $.param(data);
    var options = {
      type: "POST",
      url: '/oauth/access_token',
      data: query_string,
      contentType: "application/x-www-form-urlencoded",
      dataType: 'json',
      resolve: function (response) {
        if (response['access_token']) {
          var token = response['access_token'];
          localStorage.setItem("uengine-iam-access-token", token);
          return response;
        } else {
          console.log('login failed');
          localStorage.removeItem("uengine-iam-access-token");
          return response;
        }
      }
    };
    return this.send(options);
  },
  validateToken: function (token) {
    token = token ? token : localStorage.getItem("uengine-iam-access-token");
    var options = {
      type: "GET",
      url: '/oauth/token_info?access_token=' + token,
      dataType: "json",
      async: true
    };
    return this.send(options);
  },
  getUser: function (userName) {
    var options = {
      type: "GET",
      url: '/rest/v1//user/search/findByUserName?userName=' + userName,
      dataType: "json"
    };
    return this.send(options);
  },
  getUserSearch: function (userName, page, size) {
    var data = {
      userName: userName,
      page: page,
      size: size
    };
    console.log('getUserSearch', data);
    var url = userName ? '/rest/v1/user/search/findLikeUserName' : '/rest/v1/user';
    var options = {
      type: "GET",
      url: url,
      dataType: 'json',
      data: data,
      resolve: function (response, status, xhr) {
        var total = parseInt(xhr.getResponseHeader('x-uengine-pagination-totalnbrecords'));
        var offset = parseInt(xhr.getResponseHeader('x-uengine-pagination-currentoffset'));
        console.log(total, offset);
        return {
          data: response,
          total: total,
          offset: offset,
          page: page,
          size: size
        };
      }
    };
    return this.send(options);
  },
  createUser: function (data) {
    var options = {
      type: "POST",
      url: '/rest/v1/user',
      data: JSON.stringify(data),
      contentType: "application/json",
      dataType: 'json'
    };
    return this.send(options);
  },
  updateUser: function (userName, data) {
    data['userName'] = userName;
    var options = {
      type: "PUT",
      url: '/rest/v1/user',
      data: JSON.stringify(data),
      contentType: "application/json",
      dataType: "json"
    };
    return this.send(options);
  },
  deleteUser: function (userName) {
    var options = {
      type: "DELETE",
      url: '/rest/v1/user?userName=' + userName
    };
    return this.send(options);
  },
  createUserAvatarByFormData: function (file, contentType, userName) {
    var formData = new FormData();
    formData.append('userName', userName);
    formData.append('contentType', contentType);
    formData.append('file', file);
    var options = {
      type: "POST",
      url: '/rest/v1/avatar/formdata',
      data: formData,
      contentType: false,
      processData: false,
      dataType: 'json'
    };
    return this.send(options);
  },
  deleteUserAvatar: function (userName) {
    var options = {
      type: "DELETE",
      url: '/rest/v1/avatar?userName=' + userName
    };
    return this.send(options);
  },
  getUserAvatarUrl: function (userName) {
    return this.baseUrl + '/rest/v1/avatar?userName=' + userName;
  },
  signUp: function (redirect_url, userData) {
    var clientKey = localStorage.getItem('uengine-iam-client-key');
    var data = {
      clientKey: clientKey,
      redirect_url: redirect_url,
      oauthUser: userData
    };
    var options = {
      type: "POST",
      url: '/rest/v1/user/signup',
      data: JSON.stringify(data),
      contentType: "application/json",
      dataType: 'json'
    };
    return this.send(options);
  },
  signUpVerification: function (token) {
    var clientKey = localStorage.getItem('uengine-iam-client-key');
    var options = {
      type: "GET",
      url: '/rest/v1/user/signup/verification?clientKey=' + clientKey + '&token=' + token,
      dataType: "json"
    };
    return this.send(options);
  },
  signUpAccept: function (token) {
    var clientKey = localStorage.getItem('uengine-iam-client-key');
    var data = {
      clientKey: clientKey,
      token: token
    };
    var options = {
      type: "POST",
      url: '/rest/v1/user/signup/accept',
      data: JSON.stringify(data),
      contentType: "application/json",
      dataType: 'json'
    };
    return this.send(options);
  },

  forgotPassword: function (redirect_url, userName) {
    var clientKey = localStorage.getItem('uengine-iam-client-key');
    var data = {
      clientKey: clientKey,
      redirect_url: redirect_url,
      oauthUser: {
        userName: userName
      }
    };
    var options = {
      type: "POST",
      url: '/rest/v1/user/forgot',
      data: JSON.stringify(data),
      contentType: "application/json",
      dataType: 'json'
    };
    return this.send(options);
  },
  forgotPasswordVerification: function (token) {
    var clientKey = localStorage.getItem('uengine-iam-client-key');
    var options = {
      type: "GET",
      url: '/rest/v1/user/forgot/verification?clientKey=' + clientKey + '&token=' + token,
      dataType: "json"
    };
    return this.send(options);
  },
  forgotPasswordAccept: function (token, password) {
    var clientKey = localStorage.getItem('uengine-iam-client-key');
    var data = {
      clientKey: clientKey,
      token: token,
      password: password
    };
    var options = {
      type: "POST",
      url: '/rest/v1/user/forgot/accept',
      data: JSON.stringify(data),
      contentType: "application/json",
      dataType: 'json'
    };
    return this.send(options);
  },

  getClient: function (clientKey) {
    var options = {
      type: "GET",
      url: '/rest/v1/client/' + clientKey,
      dataType: "json"
    };
    return this.send(options);
  },
  getAllClient: function () {
    var options = {
      type: "GET",
      url: '/rest/v1/client',
      dataType: "json"
    };
    return this.send(options);
  },
  getAllScope: function () {
    var options = {
      type: "GET",
      url: '/rest/v1/scope',
      dataType: "json"
    };
    return this.send(options);
  },
  getScope: function (scopeName) {
    var options = {
      type: "GET",
      url: '/rest/v1/scope/' + scopeName,
      dataType: "json"
    };
    return this.send(options);
  },
  getAllTemplate: function (clientKey) {
    var options = {
      type: "GET",
      url: '/rest/v1/client/' + clientKey + '/template',
      dataType: 'json'
    };
    return this.send(options);
  },
  send: function (options) {
    var caller = arguments.callee.caller.name;
    var me = this;
    var deferred = $.Deferred();
    var ajaxOptions = {
      type: options.type,
      url: me.baseUrl + options.url
    };
    if (options.processData || typeof options.processData == 'boolean') {
      ajaxOptions.processData = options.processData;
    }

    if (options.dataType) {
      ajaxOptions.dataType = options.dataType;
    }
    if (options.contentType || typeof options.contentType == 'boolean') {
      ajaxOptions.contentType = options.contentType;
    }
    if (typeof options.async == 'boolean' && !options.async) {
      ajaxOptions.async = false;
    }
    if (options.data) {
      ajaxOptions.data = options.data;
    }
    var promise = $.ajax(ajaxOptions);
    promise.done(function (response, status, xhr) {
      console.log(caller + ' success');
      if (options.resolve) {
        response = options.resolve(response, status, xhr);
      }
      deferred.resolve(response);
    });
    promise.fail(function (response, status, errorThrown) {
      console.log(caller + ' failed');
      console.log(response, status, errorThrown);
      if (options.reject) {
        response = options.reject(response, status, errorThrown);
      }
      deferred.reject(response);
    });
    return deferred.promise();
  }
}
;
IAM.prototype.constructor = IAM;
