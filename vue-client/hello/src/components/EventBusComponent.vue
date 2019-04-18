<template>
  <div>
    <td class="text-xs-right">
      <v-edit-dialog dark large lazy persistent @save="addProduct()" @open="onOpenAdd()">
        <div>
          <v-btn color="error" small light>add</v-btn>
        </div>
        <template v-slot:input>
          <div class="mt-3 title">Update</div>
        </template>
        <template v-slot:input>
          <v-text-field v-model="newItem.name" label="name" single-line counter autofocus></v-text-field>
          <v-text-field v-model="newItem.price" label="price" single-line counter autofocus></v-text-field>
          <v-text-field v-model="newItem.type" label="type" single-line counter autofocus></v-text-field>
        </template>
      </v-edit-dialog>
    </td>
    <v-data-table :headers="headers" :items="productList" class="elevation-1" dark>
      <template v-slot:items="props">
        <td class="text-xs-right">{{ props.item.productId }}</td>
        <td class="text-xs-right">{{props.item.name}}</td>
        <td class="text-xs-right">{{ props.item.sellerId }}</td>
        <td class="text-xs-right">{{ props.item.price }}</td>
        <td class="text-xs-right">{{ props.item.illustration }}</td>
        <td class="text-xs-right">{{ props.item.type }}</td>

        <td class="text-xs-right">
          <v-btn color="error" small light v-on:click="deleteProduct(props.item.productId)">delete</v-btn>
        </td>
        <td class="text-xs-right">
          <v-edit-dialog dark large lazy persistent @save="update(props.item)">
            <div>
              <v-btn color="success" small light>edit</v-btn>
            </div>
            <template v-slot:input>
              <div class="mt-3 title">Update</div>
            </template>
            <template v-slot:input>
              <v-text-field v-model="props.item.name" label="name" single-line counter autofocus></v-text-field>
              <v-text-field v-model="props.item.price" label="price" single-line counter autofocus></v-text-field>
              <v-text-field v-model="props.item.type" label="type" single-line counter autofocus></v-text-field>
            </template>
          </v-edit-dialog>
        </td>
      </template>
    </v-data-table>
  </div>
</template>


<script>
import EventBus from "vertx3-eventbus-client";

export default {
  name: "EventBusComponent",
  data: function() {
    return {
      productList: [],
      newItem: {
        price: "",
        name: "",
        type: ""
      },
      headers: [
        {
          text: "productId",
          align: "left",
          sortable: false,
          value: "productId"
        },
        {
          text: "name",
          align: "left",
          sortable: false,
          value: "name"
        },
        { text: "sellerId", value: "sellerId" },
        { text: "price", value: "price" },
        { text: "illustration", value: "illustration" },
        { text: "type", value: "type" }
      ]
    };
  },
  created: function() {
    var vm = this;

    vm.getProductList();

    var sock = new EventBus("http://localhost:8081/eventbus");
    sock.onopen = function() {
      console.log("open");
      //sock.send('test');

      sock.registerHandler("product-created-client", function(err, message) {
        console.log("received a message: " + message);

        console.log(message);

        console.log(app);
        vm.productList.push(message.body);
      });

      sock.registerHandler("product-created", function(err, message) {
        console.log("received a message: " + message);
        console.log(message);

        console.log(app);

        vm.productList.unshift(message.body);
      });

      sock.registerHandler("product-updated", function(err, message) {
        console.log("received a message: " + message);
        console.log(message);

        console.log(app);
        var returnedProduct = message.body;

        var editedProduct = vm.productList.filter(
          p => p.productId == returnedProduct.productId
        );

        var index = vm.productList.findIndex(
          p => p.productId == returnedProduct.productId
        );
        vm.productList.splice(index, 1, returnedProduct);

        //vm.productList.push(message.body);
      });

      sock.registerHandler("product-deleted", function(err, message) {
        console.log("received a message: " + message);
        console.log(message);
        var productId = message.body.productId;

        console.log(app);

        vm.productList = vm.productList.filter(p => p.productId != productId);

        //vm.productList = vm.$_.without(vm.productList, deletedProduct);
      });
    };
  },
  methods: {
    getProductList: function() {
      var vm = this;

      this.$http
        .get("products")
        .then(response => {
          vm.productList = response.data;
        })
        .catch(e => {
          console.error(e);
        });
    },
    deleteProduct: function(productId) {
      this.$http
        .delete("products/" + productId)
        .then(response => {
          console.log(response);
        })
        .catch(e => {
          console.error(e);
        });
    },
    update: function(item) {
      this.$http
        .put("products/" + item.productId, item)
        .then(response => {
          console.log(response);
        })
        .catch(e => {
          console.error(e);
        });
    },
    addProduct: function() {
      var vm = this;
      this.$http
        .post("products", vm.newItem)
        .then(response => {
          console.log(response);
        })
        .catch(e => {
          console.error(e);
        });
    },
    onOpenAdd: function() {
      var vm = this;

      vm.newItem = {
        price: "",
        name: "",
        type: ""
      };
    }
  }
};
</script>
