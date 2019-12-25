package top.trister.shipper.core.dsl

import top.trister.shipper.core.api.handler.Handler
import top.trister.shipper.core.builder.HandlerBuilder

class DSLDelegate<T extends Handler> extends PropertiesDelegate {

    Map<String, HandlerDefinition<T>> handlerDefinitions = [:]
    Closure closure
    HandlerBuilder handlerBuilder
    List<HandlerDefinition<T>> handlerList = new ArrayList<>();

    List<HandlerDefinition<T>> getAndClear() {
        List<HandlerDefinition<T>> result = handlerList;
        handlerList = new ArrayList<>(handlerDefinitions.size())
        return result
    }

    def methodMissing(String name, Object obj) {//一个已知bug,如果在层中使用if语句选择handler实现,则会出现意料之外的情况
        Object[] args = obj
        for (arg in args) {
            if (arg instanceof Closure) {
                if (!handlerDefinitions.containsKey(name)) {
                    def handler = handlerBuilder.builderHandler(name)
                    def definition = new HandlerDefinition()
                    Closure closure = arg
                    def delegate = new HandlerDelegate(handler, handlerBuilder)
                    definition.handlerClosure = closure
                    definition.handler = handler
                    definition.name = name
                    closure.delegate = delegate
                    closure.resolveStrategy = Closure.DELEGATE_ONLY
                    handlerDefinitions[name] = definition
                    handlerList << definition
                } else {
                    handlerList << handlerDefinitions[name]
                }
            } else {
                Object.methodMissing(name, arg)
            }
        }
    }

    @Override
    String toString() {
        return "${this.class} [ handlerDefinitions :$handlerDefinitions]"
    }
}

