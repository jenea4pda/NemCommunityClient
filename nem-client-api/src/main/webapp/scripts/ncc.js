"use strict";

define(function(require) {
    var $ = require('jquery');
    var Ractive = require('ractive');
    var Mustache = require('mustache');
    var tooltipster = require('tooltipster');
    var Utils = require('Utils');
    var NccModal = require('NccModal');
    var ConfirmModal = require('ConfirmModal');
    var InputModal = require('InputModal');
    var SettingsModal = require('SettingsModal');
    var SendNemModal = require('SendNemModal');
    var TransactionConfirmModal = require('TransactionConfirmModal');

    var NccRactive = Ractive.extend({
        el: document.body,
        template: '#template',
        components: {
            errorModal: NccModal,
            messageModal: NccModal,
            confirmModal: ConfirmModal,
            inputModal: InputModal,
            settingsModal: SettingsModal,
            sendNemModal: SendNemModal,
            clientInfoModal: NccModal,
            transactionDetailsModal: NccModal,
            transactionConfirmModal: TransactionConfirmModal
        },
        computed: {
            allAccounts: function() {
                return [this.get('wallet.primaryAccount')].concat(this.get('wallet.otherAccounts'));
            },
            appStatus: function() {
                switch (this.get('nccStatus.code')) {
                    case null:
                    case undefined:
                    case Utils.config.STATUS_UNKNOWN:
                        return {
                            type: 'critical',
                            message: this.get('texts.common.appStatus.nccUnknown')
                        };
                    case  Utils.config.STATUS_STOPPED:
                        return {
                            type: 'critical',
                            message: this.get('texts.common.appStatus.nccUnavailable')
                        };
                    case  Utils.config.STATUS_STARTING:
                        return {
                            type: 'warning',
                            message: this.get('texts.common.appStatus.nccStarting')
                        };
                    default: // probably RUNNING
                        switch (this.get('nisStatus.code')) {
                            case null:
                            case undefined:
                            case Utils.config.STATUS_UNKNOWN:
                                return {
                                    type: 'critical',
                                    message: this.get('texts.common.appStatus.nisUnknown')
                                };
                            case Utils.config.STATUS_STOPPED:
                                return {
                                    type: 'critical',
                                    message: this.get('texts.common.appStatus.nisUnavailable')
                                };
                            case Utils.config.STATUS_STARTING:
                                return {
                                    type: 'warning',
                                    message: this.get('texts.common.appStatus.nisStarting')
                                };
                            case Utils.config.STATUS_RUNNING:
                                if (this.get('status.booting')) {
                                    return {
                                        type: 'warning',
                                        message: this.get('texts.common.appStatus.booting')
                                    };
                                } else {
                                    return {
                                        type: 'warning',
                                        message: this.get('texts.common.appStatus.notBooted')
                                    };
                                }
                            case Utils.config.STATUS_BOOTING:
                                return {
                                    type: 'warning',
                                    message: this.get('texts.common.appStatus.booting')
                                };
                            case Utils.config.STATUS_BOOTED:
                                var lastBlockBehind = this.get('nis.nodeMetaData.lastBlockBehind');
                                if (lastBlockBehind !== 0) {
                                    if (!lastBlockBehind) {
                                        return {
                                            type: 'warning',
                                            message: this.get('texts.common.appStatus.nisInfoNotAvailable')
                                        };
                                    } else {
                                        var daysBehind = Math.floor(this.get('nis.nodeMetaData.lastBlockBehind') / (60 * 1440));
                                        var daysBehindText;
                                        switch (daysBehind) {
                                            case 0:
                                                daysBehindText = this.get('texts.common.appStatus.daysBehind.0');
                                                break;
                                            case 1:
                                                daysBehindText = this.get('texts.common.appStatus.daysBehind.1');
                                                break;
                                            default:
                                                daysBehindText = this.fill(this.get('texts.common.appStatus.daysBehind.many'), daysBehind);
                                                break;
                                        }

                                        return {
                                            type: 'warning',
                                            message: this.fill(this.get('texts.common.appStatus.synchronizing'), this.get('nis.nodeMetaData.nodeBlockChainHeight'), daysBehindText)
                                        };
                                    }
                                } else {
                                    return {
                                        type: 'message',
                                        message: this.get('texts.common.appStatus.synchronized')
                                    };
                                }

                                return {
                                    type: 'warning',
                                    message: this.get('texts.common.appStatus.synchronizing')
                                };
                            case Utils.config.STATUS_SYNCHRONIZED:
                                return {
                                    type: 'message',
                                    message: this.get('texts.common.appStatus.synchronized')
                                };
                        }
                }
            },
            nodeBooting: function() {
                return this.get('status.booting') || this.get('nisStatus.code') === Utils.config.STATUS_BOOTING;
            },
            nodeBooted: function() {
                var nisStatus = this.get('nisStatus.code');
                return nisStatus === Utils.config.STATUS_BOOTED || nisStatus === Utils.config.STATUS_SYNCHRONIZED;
            },
            nisUnavailable: function() {
                return this.get('nisStatus.code') === Utils.config.STATUS_STOPPED;
            },
            enableManualBoot: function() {
                return !(this.get('nisUnavailable') || this.get('nodeBooting') || this.get('nodeBooted'));
            },
            nisSynchronized: function() {
                // 5 status code is not implemented yet
                // so we cannot completely rely on NIS status code
                return this.get('nisStatus.code') === Utils.config.STATUS_SYNCHRONIZED || this.get('nis.nodeMetaData.lastBlockBehind') === 0;
            }
        },
        ajaxError: function(jqXHR, textStatus, errorThrown) {
            this.showError(textStatus, errorThrown);
        },
        apiPath: function(api) {
            return '../api/' + api;
        },
        checkSuccess: function(data, silent, settings) {
            var self = this;
            function showError(faultId, error) {
                if (settings && settings.altFailCb) {
                    var noErrorModal = settings.altFailCb(faultId, error);
                }
                if (!silent && !noErrorModal) {
                    self.showError(faultId);
                }
                return false;
            }

            if (undefined !== data.error) {
                switch (data.status) {
                    case 200:
                        break;

                    case 601: // NODE_ALREADY_BOOTED
                        silent = true;

                    default:
                        return showError(data.status);
                }
            }

            return true;
        },
        syncRequest: function(url) {
            return $.ajax(url, {
                async: false,
                type: 'GET',
                error: this.ajaxError
            }).responseText;
        },
        /**
         * @param {string} type
         * @param {string} api
         * @param {object} requestData
         * @param {string} success
         */
        _ajaxRequest: function(type, api, requestData, successCb, settings, silent) {
            var self = this;
            successCb = successCb || function() {};

            // set the dataType to 'text' instead of 'json' because  NCC will return "" (empty string)
            // from void functions, but JQuery treats this as invalid JSON
            var s = {
                contentType: 'application/json',
                dataType: 'text',
                type: type,
                data: requestData ? JSON.stringify(requestData) : undefined,
                error: function (jqXHR, textStatus, errorThrown) {
                    // first check the success (in case this is a new API)
                    var data = jqXHR.responseText ? JSON.parse(jqXHR.responseText) : {};
                    if (!self.checkSuccess(data, silent, settings))
                        return;

                    // since we are in an error callback, handle the error as an unknown ajax error
                    return silent ? [] : self.ajaxError(jqXHR, textStatus, errorThrown);
                },
                success: function(data) {
                    // if the data is an empty string, emulate original NCC behavior by returning { ok: 1 }
                    if (!data) {
                        successCb({ ok: 1 });
                        return;
                    }

                    // otherwise, parse the json check the success (for legacy API handling)
                    var parsedData = $.parseJSON(data);
                    if (self.checkSuccess(parsedData, silent, settings)) {
                        successCb(parsedData);
                    }
                }
            };
            $.extend(s, settings);
            $.ajax(this.apiPath(api), s);
        },
        getRequest: function(api, successCb, settings, silent) {
            return this._ajaxRequest('get', api, undefined, successCb, settings, silent);
        },
        postRequest: function(api, requestData, successCb, settings, silent) {
            return this._ajaxRequest('post', api, requestData, successCb, settings, silent);
        },
        getModal: function(modalName) {
            return this.findComponent(modalName + 'Modal');
        },
        showError: function(errorId, message) {
            var modal = this.getModal('error');
            if (!message && typeof errorId === 'number') {
                message = this.get('texts.faults.' + errorId);
            }
            modal.set({
                errorId: errorId,
                message: message || 'Unknown error'
            });
            modal.open();
        },
        showMessage: function(title, message, closeCallback) {
            var modal = this.getModal('message');
            modal.set({
                modalTitle: title,
                message: message
            });
            modal.open();
            if (closeCallback) {
                var ob = modal.observe('isActive', function(newValue, oldValue) {
                    if (newValue === false) {
                        closeCallback();
                        ob.cancel();
                    }
                });
            }
        },
        showConfirmation: function(title, message, callbacks, actions) {
            var modal = this.getModal('confirm');
            modal.set({
                modalTitle: title,
                message: message,
                callbacks: callbacks,
                actions: actions || [
                    {
                        action: 'no',
                        label: ncc.get('texts.modals.confirmDefault.no'),
                        actionType: 'secondary'
                    },
                    {
                        action: 'yes',
                        label: ncc.get('texts.modals.confirmDefault.yes'),
                        actionType: 'primary'
                    }
                ]
            });
            modal.open();
        },
        showInputForm: function(title, message, fields, initValues, submitCb, submitLabel, processingLabel) {
            var modal = this.getModal('input');
            modal.set({
                fields: null,
                values: null
            });

            modal.set({
                modalTitle: title,
                message: message,
                fields: fields,
                values: initValues,
                submitCb: submitCb,
                submitLabel: submitLabel,
                processingLabel: processingLabel
            });
            modal.open();
        },
        toggleOn: function(id) {
            Utils.toggleOn(this, id);
        },
        toggleOff: function(id) {
            Utils.toggleOff(this, id);
        },
        fill: function(template) {
            // The first argument could be whether it should return the HTML decoded version
            if (typeof arguments[0] === 'boolean') {
                var decode = arguments[0];
                Array.prototype.splice.call(arguments, 0, 1);
                template = arguments[0];

                if (decode) {
                    return Utils.htmlDecode(Mustache.render(template, arguments));
                } else {
                    return Mustache.render(template, arguments);
                }
            } else {
                return Mustache.render(template, arguments);
            }
        },
        
        globalSetup: function() {
            require(['draggable'], function() {
                $('.modal').draggable({
                    handle: '.modal-head'
                });
            });
        },
        loadPage: function(page, params, isBack, isInit) {
            var self = this;

            // We use require(string) instead of require(array, function) to make page loading process synchronous
            // require(string) needs dependencies to be loaded before being used
            // So we have to load all layout files and templates first
            var layouts = [];
            var loadLayout = function(layoutName) {
                require([layoutName], function(layout) {
                    layouts.unshift(layout);
                    require([layout.template], function() {
                        if (layout.parent) {
                            loadLayout(layout.parent);
                        } else {
                            replaceLayouts();
                        }
                    });
                });
            };

            var replaceLayouts = function() {
                var oldParams = ncc.get('params');
                var paramsChanged = JSON.stringify(oldParams) !== JSON.stringify(params);

                for (var i = 0; i < layouts.length; i++) {
                    var layout = layouts[i];
                    var keypath = 'layout.' + i;
                    var currentLayout = self.get(keypath);

                    if (!layout.alreadyInit && layout.initOnce) {
                        layout.initOnce();
                        layout.alreadyInit = true;
                    }
                        
                    if (paramsChanged && layout.paramsChanged) {
                        var abort = layout.paramsChanged(params);
                        if (abort) return;
                    }

                    if (!currentLayout || (currentLayout.name !== layout.name)) {
                        var template = require(layout.template);
                        if (currentLayout && currentLayout.leave) {
                            $.each(currentLayout.leave, function() {
                                this.apply(currentLayout);
                            });
                        }

                        // Init
                        if (layout.initEverytime) {
                            var abort = layout.initEverytime(params);
                            if (abort) return;
                        }

                        self.set(keypath, null);
                        self.partials[i] = template;
                        self.set(keypath, layout);

                        // Setup
                        if (!layout.alreadySetup && layout.setupOnce) {
                            layout.setupOnce();
                            layout.alreadySetup = true;
                        }
                        if (layout.setupEverytime) {
                            var abort = layout.setupEverytime(params);
                            if (abort) return;
                        }
                    }
                }

                self.globalSetup();

                // Clear the old layouts
                var currLayouts = self.get('layout');
                if (currLayouts && currLayouts.length) {
                    for (i = i + 1; i < currLayouts.length; i++) {
                        self.set('layout.' + i, null);
                    }
                }

                if (!isBack) {
                    var queryString = '';
                    if (params) {
                        queryString = Utils.toQueryString(params);
                    } else if (isInit) {
                        queryString = location.search;
                        params = Utils.queryStringToJson(location.search);
                    }
                    var url = layouts[layouts.length - 1].url + queryString;
                    
                    if (isInit) {
                        history.replaceState({ page: page, params: params }, 'entry', url);
                    } else {
                        history.pushState({ page: page, params: params }, null, url);
                    }
                }
                ncc.set('params', params);
            };

            loadLayout(page);
        },
        oncomplete: function(options) {
            var self = this;

            require(['languages'], function(languages) {
                self.set('languages', languages);
                self.observe('settings.language', function(newValue) {
					newValue = newValue || Utils.config.defaultLanguage;
                    for (var i = 0; i < languages.length; i++) {
                        if (languages[i].id.toLowerCase() === newValue.toLowerCase()) {
                            self.set('texts', languages[i].texts);
                            if (undefined === self.get('nis.nodeMetaData.maxBlockChainHeight')) {
		    					self.set('nis.nodeMetaData.nodeBlockChainHeight', self.get('texts.dashboard.transactions.unknown')) // after opening the wallet the node meta data is not available yet
		    				}
                            return;
                        }
                    }
                });
            });

            this.on({
                redirect: function(e, page, params) {
                    this.loadPage(page, params);
                },
                openModal: function(e, id) {
                    this.getModal(id).open();
                },
                shutdown: function() {
                    var self = this;
                    this.showConfirmation(ncc.get('texts.modals.shutdown.title'), ncc.get('texts.modals.shutdown.message'), {
                        yes: function() {
                            self.loadPage('start');
                        }
                    });
                },
                selectFile: function(e, id) {
                    var input = this.nodes[id];
                    var evt = document.createEvent("MouseEvents");
                    evt.initEvent('click', true, false);
                    input.dispatchEvent(evt);
                },
                registerToolTip: function(e) {
                    if (!$(e.node).data('tooltipster-ns')) {
                        var $node = $(e.node);
                        $node.tooltipster({
                            position: 'bottom',
                            delay: 50,
                            functionBefore: function($origin, continueTooltip) {
                                var title = $origin.attr('title');
                                if (title) {
                                    $origin.tooltipster('content', title);
                                    $origin.removeAttr('title');
                                }
                                continueTooltip();
                            }
                        });
                        $node.tooltipster('show');
                    }
                },
                set: function(e, keypath, val) {
                    this.set(keypath, val);
                }
            });

            this.set('fill', this.fill);
            this.set('formatCurrency', Utils.formatCurrency);

            this.global = {
                $window: $(window),
                $document: $(document),
                $html: $('html'),
                $body: $('body')
            };
        }
    });

    window.ncc = new NccRactive();
    return ncc;
});
