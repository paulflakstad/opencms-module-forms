# OpenCms module: Forms

This module will enable you to create basic online forms, and overviews of data collected via those forms.

Data collected via these forms are stored in a database, and can be easily exported via a form data view.

## Dependencies:
- [no.npolar.util](https://github.com/paulflakstad/no.npolar.util)
- no.npolar.common.forms (see the [src-lib folder](https://github.com/paulflakstad/opencms-module-forms/tree/master/src-lib/no/npolar/common/forms))
- [jsoup](https://jsoup.org) (see the [module lib folder](https://github.com/paulflakstad/opencms-module-forms/tree/master/src-module/system/modules/no.npolar.common.forms/lib))

## Notes:

Multilanguage support is so-so. You _can_ get away with mixing multiple languages in a single database table, but these forms aren't really intended for doing that. Instead, consider storing collected form data from each language in its own table. (This is easy to do: Simply make sure the table name – which you should always define anyway – is unique also per language. For example, "MYFORM_EN" and "MYFORM_IT" for English and Italian data, respectively.)

Because each form is strongly tied to a database table, there are a few things you'll need to know.

You should have some SQL knowledge, as you may have to perform basic database operations. This is true in particular if you want to: 
- Make fundamental changes to a form that has already collected data at least once. For example, if you add a new field to your form, or change the type of an existing field, you will make the form and its backing table incompatible. To fix the incompatibility, you'll need to modify the form's backing database table manually. Alternatively, to avoid making such changes, you can make a copy of the form instead of changing it. (This is especially relevant if you're changing the type of an existing field, but note that it will result in a new backing database table, so your old and new data will now be separated.) If you're adding/removing fields, modifying the database table is recommended.
- Delete a form's backing database table. This is typically relevant only when deleting its associated form.
