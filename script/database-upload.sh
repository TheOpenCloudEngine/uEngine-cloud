CURRENT_DIR="$(dirname $(pwd))"

cd $CURRENT_DIR/cloud-config-repository
git init
git remote add origin http://gitlab.pas-mini.io/root/cloud-config-repository.git

git add .
git commit -m "Initial commit"
git push -u origin master
rm -rf .git

cd $CURRENT_DIR/template-springboot
git init
git remote add origin http://gitlab.pas-mini.io/root/template-springboot.git
git add .
git commit -m "Initial commit"
git push -u origin master
rm -rf .git

cd $CURRENT_DIR/template-vuejs
git init
git remote add origin http://gitlab.pas-mini.io/root/template-vuejs.git
git add .
git commit -m "Initial commit"
git push -u origin master
rm -rf .git

cd $CURRENT_DIR/template-zuul
git init
git remote add origin http://gitlab.pas-mini.io/root/template-zuul.git
git add .
git commit -m "Initial commit"
git push -u origin master
rm -rf .git

cd $CURRENT_DIR/template-iam
git init
git remote add origin http://gitlab.pas-mini.io/root/template-iam.git
git add .
git commit -m "Initial commit"
git push -u origin master
rm -rf .git