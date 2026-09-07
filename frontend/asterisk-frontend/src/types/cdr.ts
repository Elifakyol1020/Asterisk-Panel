export interface CdrRecord {
  id: string; tenantId: number | null; sequence: number; uniqueId: string; linkedId: string | null
  src: string; dst: string; srcName: string | null; dstName: string | null
  context: string | null; channel: string | null; dstChannel: string | null
  startTime: string; answerTime: string | null; endTime: string
  duration: number; billsec: number; disposition: string; recordingPath: string | null
}
