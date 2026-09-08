import { http } from '@/utils/request'
import type { AddressDTO, AddressVO, UserProfileDTO, UserVO } from '@/types/user'

export const userApi = {
  me: () => http.get<unknown, UserVO>('/users/me'),
  updateMe: (dto: UserProfileDTO) => http.put<unknown, UserVO>('/users/me', dto),
  addresses: () => http.get<unknown, AddressVO[]>('/users/me/addresses'),
  addAddress: (dto: AddressDTO) => http.post<unknown, AddressVO>('/users/me/addresses', dto),
  updateAddress: (id: number, dto: AddressDTO) =>
    http.put<unknown, AddressVO>(`/users/me/addresses/${id}`, dto),
  deleteAddress: (id: number) => http.delete<unknown, void>(`/users/me/addresses/${id}`),
  setDefaultAddress: (id: number) => http.put<unknown, void>(`/users/me/addresses/${id}/default`),
}
