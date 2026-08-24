name: CodeQL

on:
  push:
  pull_request:
  schedule:
    - cron: '17 3 * * 1'

permissions:
  contents: read
  security-events: write

jobs:
  analyze:
    if: github.event_name != 'pull_request' || github.event.pull_request.head.repo.full_name == github.repository
    runs-on: ubuntu-latest
    permissions:
      actions: read
      contents: read
      security-events: write
    steps:
      - uses: actions/checkout@11bd71901bbe5b1630ceea73d27597364c9af683 # v4.2.2
        with:
          persist-credentials: false
      - uses: github/codeql-action/init@f35333b910470a5408cb081b68f0701254a7d27b # v3.28.18
        with:
          languages: java
      - uses: actions/setup-java@c5195efecf7bdfc987ee8bae7a71cb8b11521c00 # v4.7.1
        with:
          distribution: temurin
          java-version: '17'
      - run: mvn -B -ntp -DskipTests package
      - uses: github/codeql-action/analyze@f35333b910470a5408cb081b68f0701254a7d27b # v3.28.18
