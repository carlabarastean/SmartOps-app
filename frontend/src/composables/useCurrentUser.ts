import { ref } from 'vue'
import type { User } from '../types'

const currentUser = ref<User | null>(null)

export function useCurrentUser() {
  const setCurrentUser = (user: User | null) => {
    currentUser.value = user
  }

  return {
    currentUser,
    setCurrentUser,
  }
}

