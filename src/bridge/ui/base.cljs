(ns bridge.ui.base)

(defmulti load-on-view :view)

(defmethod load-on-view :default [_]
  nil)

(defmulti view :view)
