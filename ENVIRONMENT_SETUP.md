# Environment Setup

## Java 11 (Backend)

This project uses [jenv](https://www.jenv.be/) to manage Java versions. The `.java-version` file auto-selects Java 11.

**macOS:**
```bash
brew install jenv openjdk@11
jenv add /opt/homebrew/opt/openjdk@11/libexec/openjdk.jdk/Contents/Home
```

**Linux (Debian/Ubuntu):**
```bash
sudo apt install openjdk-11-jdk
git clone https://github.com/jenv/jenv.git ~/.jenv
echo 'export PATH="$HOME/.jenv/bin:$PATH"' >> ~/.bashrc
jenv add /usr/lib/jvm/java-11-openjdk-amd64
```

Then initialize jenv in your shell (`~/.zshrc` or `~/.bashrc`):
```bash
echo 'eval "$(jenv init -)"' >> ~/.zshrc
source ~/.zshrc
```

Verify from the project root:
```bash
java -version  # Should output: openjdk version "11.x.x"
```

## Node.js 16 (Frontend)

This project uses [nvm](https://github.com/nvm-sh/nvm) to manage Node versions. The `frontend/.nvmrc` file auto-selects Node 16.

**macOS & Linux:**
```bash
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.7/install.sh | bash
nvm install 16
```

Verify from the `frontend/` directory:
```bash
cd frontend && nvm use && node --version  # Should output: v16.x.x
```
