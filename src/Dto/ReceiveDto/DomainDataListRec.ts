export interface DomainDataItem {
  domainId: number
  domainName: string
  domainUrl: string
  createdAt: string
  domainImageUrl: string
  domainDescription: string
  trainDataCountList: number[]
  commentTotal: number
  modelTotal: number
  merchantTotal: number
}

export interface DomainDataListRec {
  sourceNameList: string[]
  domainDataList: DomainDataItem[]
}
