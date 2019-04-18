var sock = new EventBus('http://localhost:8081/eventbus');
sock.onopen = function () {
    console.log('open');
    //sock.send('test');

    sock.registerHandler('product-created-client', function (err, message) {

        console.log('received a message: ' + message);

        console.log(message);

        console.log(app);
        app.productList.push(message.body);
    
    });

    sock.registerHandler('product-created', function (err, message) {

        console.log('received a message: ' + message);
        console.log(message);

        console.log(app);
        app.productList.push(message.body);
    
    });
};
/*
sock.onmessage = function (e) {
    console.log('message', e.data);
    sock.close();
};

sock.onclose = function () {
    console.log('close');
};
*/
