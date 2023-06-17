const nameToColorMap = new Map<string, string>([
  ["サクラ", 'material-red'],
  ["ウメ", 'material-pink'],
  ["カエデ", 'material-purple'],
  ["シラカバ", 'material-deep-purple'],
  ["アジサイ", 'material-indigo'],
  ["ツツジ", 'material-blue'],
  ["モミジ", 'material-light-blue'],
  ["ツバキ", 'material-cyan'],
  ["マツ", 'material-teal'],
  ["モクレン", 'material-green'],
  ["タケ", 'material-light-green'],
  ["ヒイラギ", 'material-lime'],
]) as ReadonlyMap<string, string>

export const namedColorSet = (name: string): string => {
  return nameToColorMap.get(name) || 'material-grey'
}
