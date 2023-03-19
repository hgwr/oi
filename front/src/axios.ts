import axios from 'axios'
import qs from 'qs'

const instance = axios.create({
  baseURL: '/api',
  paramsSerializer: {
    serialize: (params) => {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    },
  },
})

export default instance
