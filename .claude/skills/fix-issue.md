# GitHub Issue対応手順

GitHub issueを対応してPRを作成する際の標準的な手順。

## 1. Issue確認

```bash
gh issue list
gh issue view <issue番号>
```

- issueの内容を確認し、対応範囲を把握する
- 簡単なものから対応する場合は、影響範囲が小さいものを選ぶ

## 2. ブランチ作成

```bash
git checkout main
git pull
git checkout -b fix/<issue内容を表す名前>
```

- ブランチ名は `fix/`, `feat/`, `docs/` などのprefixを使用

## 3. 実装

### 3.1 関連コードの調査

- `Grep`や`Glob`ツールで関連ファイルを探す
- 既存の実装パターンを確認する

### 3.2 コード修正

- runtimeモジュール: `runtime/src/commonMain/kotlin/io/github/fuyuz/svgicon/core/`
- gradle-plugin: `gradle-plugin/src/main/kotlin/io/github/fuyuz/svgicon/core/`
- **重要**: runtimeとgradle-pluginの両方で対応が必要な場合がある（パーサーとコード生成）

### 3.3 ビルド確認

```bash
./gradlew :runtime:compileKotlinDesktop
./gradlew :sample:compileKotlinDesktop
```

## 4. テスト追加

- テストファイル: `runtime/src/commonTest/kotlin/io/github/fuyuz/svgicon/core/`
- 新機能には必ずテストを追加

```bash
./gradlew :runtime:desktopTest --tests "*テスト名*"
```

## 5. サンプル追加

- 新機能にはサンプルアイコンを追加: `sample/src/commonMain/svgicons/`
- サンプルビルドで生成コードを確認

```bash
./gradlew :sample:generateSvgIcons --rerun-tasks
```

## 6. CHANGELOG更新

`CHANGELOG.md`の`[Unreleased]`セクションに変更を追加:

- `### Added` - 新機能
- `### Fixed` - バグ修正
- `### Changed` - 既存機能の変更

## 7. コミット・PR作成

```bash
git add <変更ファイル>
git commit -m "$(cat <<'EOF'
コミットメッセージ

- 変更点1
- 変更点2

Closes #<issue番号>
EOF
)"

git push -u origin <ブランチ名>

gh pr create --title "PRタイトル" --body "$(cat <<'EOF'
## Summary
- 変更の概要

## Changes
- 変更したファイルの説明

## Test plan
- [x] テスト項目

Closes #<issue番号>
EOF
)" --base main
```

## チェックリスト

- [ ] issueの内容を理解した
- [ ] 新しいブランチを作成した
- [ ] runtime/gradle-plugin両方の対応を確認した
- [ ] ビルドが通ることを確認した
- [ ] テストを追加・実行した
- [ ] サンプルを追加した（新機能の場合）
- [ ] CHANGELOGを更新した
- [ ] PRを作成した
