import qs from 'qs'
import { createRouter, createWebHistory } from 'vue-router'
import HomeView from './views/HomeView.vue'
import RoomView from './views/RoomView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/room',
      name: 'room',
      component: RoomView
    },
    {
      path: '/*',
      redirect: '/'
    }
  ],
  stringifyQuery: (query) => { return qs.stringify(query, { arrayFormat: 'repeat' }) }
})

export default router
