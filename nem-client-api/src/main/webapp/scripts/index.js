"use strict";

require.config({
    baseUrl: 'scripts',
    paths: {
        'layout': '../layouts',
        'jquery': 'plugins/jquery-2.1.0.min',
        'expand': 'plugins/expand',
        'modernizr': 'plugins/modernizr.custom.00846',
        'ractive': 'plugins/ractive.min',
        'tinycarousel': 'plugins/jquery.tinycarousel.min',
        'scroller': 'plugins/jquery.fs.scroller.min',
        'selecter': 'plugins/jquery.fs.selecter.min',
        'stepper': 'plugins/jquery.fs.stepper.min',
        'gridster': 'plugins/jquery.gridster.min',
        'maskedinput': 'plugins/jquery.mask.min',
        'dropit': 'plugins/dropit.min',
        'zeroClipboard': 'plugins/ZeroClipboard.min',
        'tooltipster': 'plugins/jquery.tooltipster.min',
        'mustache': 'plugins/mustache.min',
        'draggable': 'plugins/jquery-ui.draggable.min'
    },
    shim: {
        'scroller': { deps: ['jquery'] },
        'selecter': { deps: ['jquery'] },
        'stepper': { deps: ['jquery'] },
        'gridster': { deps: ['jquery'] },
        'maskedinput': { deps: ['jquery'] },
        'dropit': { deps: ['jquery'] },
        'tooltipster': { deps: ['jquery'] }
    }
});

define(['ncc', 'Utils'], function(ncc, Utils) {
    ncc.getRequest('configuration/get', function(data) {
        ncc.set('settings', data);
        ncc.loadPage(entryPage, null, false, true);
    });

    window.onpopstate = function(event) {
        ncc.loadPage(event.state.page, event.state.params, true, false);
    };

    ncc.refreshNccStatus = function(complete) {
        var success = false;
        ncc.getRequest('status', 
            function(data) {
                ncc.set('nccStatus', data);
                success = true;
            },
            {
                complete: function() {
                    if (!success) {
                        ncc.set('nccStatus.code', Utils.config.STATUS_STOPPED);
                    }

                    if (complete) complete();
                }
            },
            true
        );
    };
    ncc.refreshNccStatus();
    setInterval(ncc.refreshNccStatus, 3000);

    ncc.refreshNisStatus = function(complete) {
        var success = false;
        ncc.getRequest('node/status', 
            function(data) {
                ncc.set('nisStatus', data);
                success = true;
            },
            {
                complete: function(jqXHR) {
                    if (jqXHR.status === 0) {
                        ncc.set('nccStatus.code', Utils.config.STATUS_STOPPED);
                    } else if (!success) {
                        ncc.set('nisStatus.code', Utils.config.STATUS_STOPPED);
                    }

                    if (complete) complete();
                }
            },
            true
        );
    };
    ncc.refreshNisStatus();
    setInterval(ncc.refreshNisStatus, 3000);

    ncc.refreshAppStatus = function(complete) {
        ncc.refreshNccStatus(function() {
            ncc.refreshNisStatus(complete);
        });
    };

    (function() {
        var waitTime = 30000;
        var t = 0;

        ncc.refreshNisInfo = function() {
            clearTimeout(t);
            ncc.getRequest('info/nis',
                function(data) {
                	var lastBlockBehind = (data.nodeMetaData.maxBlockChainHeight - data.nodeMetaData.nodeBlockChainHeight) * 60;
                    ncc.set('nis', data);
                    ncc.set('nis.nodeMetaData.lastBlockBehind', lastBlockBehind < 0? 0 : lastBlockBehind);
                    if (data.nodeMetaData.maxBlockChainHeight === data.nodeMetaData.nodeBlockChainHeight) {
                        waitTime = 60000;
                    } else {
                        waitTime = 30000;
                    }
                },
                {
                    complete: function() {
                        t = setTimeout(ncc.refreshNisInfo, waitTime);
                    }
                },
                true
            );
        };

        ncc.observe('nodeBooted', function(booted) {
            if (booted) {
                ncc.refreshNisInfo();
            } else {
                clearTimeout(t);
            }
        });
    })();

    ncc.refreshChainHeight = function() {
        ncc.getRequest('/info/nis/chain/height', function(data) {
            ncc.set('blockchainHeight', data.height);
        }, null, true);
    };
    ncc.refreshChainHeight();
    setInterval(ncc.refreshChainHeight, 3000);
});