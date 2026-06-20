/**
 * デモシードに残る \\uXXXX リテラルを Unicode 文字へ復号する。
 */
/** シード文字列の \\uXXXX を Unicode に復号 */
export function displayText(value: string | null | undefined): string {
  if (!value) return ''
  if (!value.includes('\\u')) return value
  return value.replace(/\\u([0-9a-fA-F]{4})/g, (_, hex: string) =>
    String.fromCharCode(parseInt(hex, 16)),
  )
}
