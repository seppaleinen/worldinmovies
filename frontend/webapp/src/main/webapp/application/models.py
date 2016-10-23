from wtforms import Form, TextField, PasswordField, validators


class SignupForm(Form):
    username = TextField('Namn', [validators.Length(min=4, max=25)])
    password = PasswordField('Password', [
        validators.DataRequired(),
        validators.EqualTo('confirm', message='Passwords must match')
    ])
    confirm = PasswordField('Repeat Password')


class LoginForm(Form):
    username = TextField('Namn', [validators.Length(min=4, max=25)])
    password = PasswordField('Password', [validators.DataRequired()])
