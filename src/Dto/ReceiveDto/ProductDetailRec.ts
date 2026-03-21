/**
 * Product detail response DTO.
 */
export interface ProductDetailRec {
  productId: number
  name: string
  imageUrl: string | null
  details: string
  price: number
  rating: number
  commentCount: number
  merchantId: number
  merchantName: string
  domainId: number
  positiveRate: number
}
