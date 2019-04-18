<template>
  <v-app>


    <v-content>
      <EventBusComponent/>
    </v-content>
  </v-app>
</template>

<script>
import EventBusComponent from './components/EventBusComponent'

export default {
  name: 'App',
  components: {
    EventBusComponent
  },
  data () {
    return {
      //
    }
  }
}
</script>
