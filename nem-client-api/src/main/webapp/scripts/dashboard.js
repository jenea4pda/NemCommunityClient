"use strict";

 define(['jquery', 'ncc', 'NccLayout', 'Utils'], function($, ncc, NccLayout, Utils) {
    return $.extend(true, {}, NccLayout, {
        name: 'dashboard',
        url: 'dashboard.html',
        template: 'rv!layout/dashboard',
        parent: 'wallet',
        setupOnce: function() {

            // Charts
            /*
            var nemValueDataSource = [
                { month: 'Jan', BTC: 1000, USD: 2315, EUR: 3132 },
                { month: 'Feb', BTC: 3232, USD: 1234, EUR: 3164 },
                { month: 'Mar', BTC: 3146, USD: 4231, EUR: 4314 },
                { month: 'Apr', BTC: 2426, USD: 4321, EUR: 1243 },
                { month: 'May', BTC: 4323, USD: 3264, EUR: 1844 },
                { month: 'Jun', BTC: 3754, USD: 3316, EUR: 2156 },
                { month: 'July', BTC: 3421, USD: 2312, EUR: 1243 },
                { month: 'Aug', BTC: 2978, USD: 3569, EUR: 3128 }
            ];

            var btcSeries = { name: 'BTC', valueField: 'BTC', point: { color: '#074661', size: 4 } };
            var usdSeries = { name: 'USD', valueField: 'USD', point: { color: '#074661', size: 4 } };
            var eurSeries = { name: 'EUR', valueField: 'EUR', point: { color: '#074661', size: 4 } };
            var series = {
                btc: btcSeries,
                usd: usdSeries,
                eur: eurSeries
            }

            var $nemValueChart = $('#nem-value-chart');

            $nemValueChart.dxChart({
                dataSource: nemValueDataSource,
                series: btcSeries,
                commonSeriesSettings: {
                    argumentField: 'month',
                    color: '#27d782',
                    width: 1
                },
                tooltip: {
                    enabled: true,
                    font: {
                        size: 11,
                        color: '#fff'
                    }
                },
                legend: {
                    visible: false
                }
            });

            var chart = $nemValueChart.dxChart("instance");

            $('#nem-value-unit-chooser').change(function() {
                var selected = this.options[this.selectedIndex].value;
                chart.option({
                    series: series[selected]
                });
            });*/
        },
        setupEverytime: function() {
            var local = this.local;
            local.$dashboard = $('.js-dashboard');

            require(['gridster'], function() {
                local.$dashboard.gridster({
                    widget_margins: [10, 9],
                    widget_base_dimensions: [294, 177],
                    max_cols: 3,
                    /*draggable: {
                        handle: 'div.dragger'
                    }*/
                }).data('gridster').disable(); //disable tiles drag & drop
            });

            local.listeners.push(ncc.on({
                activateRemoteHarvesting: function() {
                    ncc.showInputForm(ncc.get('texts.modals.activateRemote.title'), '',
                        [   
                            {
                                name: 'wallet',
                                type: 'text',
                                readonly: true,
                                unimportant: true,
                                label: {
                                    content: ncc.get('texts.modals.activateRemote.wallet')
                                }
                            },
                            {
                                name: 'password', 
                                type: 'password',
                                label: {
                                    content: ncc.get('texts.modals.activateRemote.password')
                                }
                            },
                            {
                                name: 'account',
                                type: 'text',
                                readonly: true,
                                unimportant: true,
                                label: {
                                    content: ncc.get('texts.modals.activateRemote.account')
                                }
                            },
                            {
                                name: 'hours_due',
                                type: 'text',
                                label: {
                                    content: ncc.get('texts.modals.activateRemote.hoursDue')
                                }
                            }
                        ],
                        {
                            wallet: ncc.get('wallet.name'),
                            account: ncc.get('activeAccount.address')
                        },
                        function(values, closeModal) {
                            values.hours_due = parseInt(values.hours_due, 10);
                            ncc.postRequest('wallet/account/remote/activate', values, function(data) {
                                closeModal();
                            }, {
                                complete: function() {
                                    ncc.refreshAccount();
                                }
                            });
                        },
                        ncc.get('texts.modals.activateRemote.activate')
                    );
                },
                deactivateRemoteHarvesting: function() {
                    ncc.showInputForm(ncc.get('texts.modals.deactivateRemote.title'), '',
                        [   
                            {
                                name: 'wallet',
                                type: 'text',
                                readonly: true,
                                unimportant: true,
                                label: {
                                    content: ncc.get('texts.modals.deactivateRemote.wallet')
                                }
                            },
                            {
                                name: 'password', 
                                type: 'password',
                                label: {
                                    content: ncc.get('texts.modals.deactivateRemote.password')
                                }
                            },
                            {
                                name: 'account',
                                type: 'text',
                                readonly: true,
                                unimportant: true,
                                label: {
                                    content: ncc.get('texts.modals.deactivateRemote.account')
                                }
                            },
                            {
                                name: 'hours_due',
                                type: 'text',
                                label: {
                                    content: ncc.get('texts.modals.deactivateRemote.hoursDue')
                                }
                            }
                        ],
                        {
                            wallet: ncc.get('wallet.name'),
                            account: ncc.get('activeAccount.address')
                        },
                        function(values, closeModal) {
                            values.hours_due = parseInt(values.hours_due, 10);
                            ncc.postRequest('wallet/account/remote/deactivate', values, function(data) {
                                closeModal();
                            }, {
                                complete: function() {
                                    ncc.refreshAccount();
                                }
                            });
                        },
                        ncc.get('texts.modals.deactivateRemote.deactivate')
                    );
                },
                startRemoteHarvesting: function() {
                    ncc.showInputForm(ncc.get('texts.modals.startRemote.title'), '',
                        [   
                            {
                                name: 'wallet',
                                type: 'text',
                                readonly: true,
                                unimportant: true,
                                label: {
                                    content: ncc.get('texts.modals.startRemote.wallet')
                                }
                            },
                            {
                                name: 'password', 
                                type: 'password',
                                label: {
                                    content: ncc.get('texts.modals.startRemote.password')
                                }
                            },
                            {
                                name: 'account',
                                type: 'text',
                                readonly: true,
                                unimportant: true,
                                label: {
                                    content: ncc.get('texts.modals.startRemote.account')
                                }
                            }
                        ],
                        {
                            wallet: ncc.get('wallet.name'),
                            account: ncc.get('activeAccount.address')
                        },
                        function(values, closeModal) {
                            ncc.postRequest('wallet/account/remote/unlock', values, function(data) {
                                closeModal();
                            }, {
                                complete: function() {
                                    ncc.refreshAccount();
                                }
                            });
                        },
                        ncc.get('texts.modals.startRemote.start')
                    );
                },
                stopRemoteHarvesting: function() {
                    ncc.showInputForm(ncc.get('texts.modals.stopRemote.title'), '',
                        [   
                            {
                                name: 'wallet',
                                type: 'text',
                                readonly: true,
                                unimportant: true,
                                label: {
                                    content: ncc.get('texts.modals.stopRemote.wallet')
                                }
                            },
                            {
                                name: 'password', 
                                type: 'password',
                                label: {
                                    content: ncc.get('texts.modals.stopRemote.password')
                                }
                            },
                            {
                                name: 'account',
                                type: 'text',
                                readonly: true,
                                unimportant: true,
                                label: {
                                    content: ncc.get('texts.modals.stopRemote.account')
                                }
                            },
                            {
                                name: 'host',
                                type: 'text',
                                label: {
                                    content: ncc.get('texts.modals.stopRemote.host')
                                }
                            }
                        ],
                        {
                            wallet: ncc.get('wallet.name'),
                            account: ncc.get('activeAccount.address')
                        },
                        function(values, closeModal) {
                            ncc.postRequest('wallet/account/remote/lock', values, function(data) {
                                closeModal();
                            }, {
                                complete: function() {
                                    ncc.refreshAccount();
                                }
                            });
                        },
                        ncc.get('texts.modals.stopRemote.stop')
                    );
                }
            }));

            ncc.refreshAccount();

            /*require(['scroller'], function() {
                $('.scrollable').scroller();
            });

            require(['selecter'], function() {
                $('select').selecter();
            });

            require(['tinycarousel'], function() {
                $('.tile.news .news-preview').tinycarousel({
                    axis: 'y',
                    buttons: false,
                    bullets: true,
                    interval: true,
                    intervalTime: 4000,
                    animationTime: 700
                });
                $('.tile.messages .message-preview').tinycarousel({
                    axis: 'y',
                    buttons: false,
                    interval: true,
                    intervalTime: 4000,
                    animationTime: 700
                });
            });*/
        }
    });
});