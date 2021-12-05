package main.kotlin
import java.util.*
import kotlin.math.E
import kotlin.math.PI
import kotlin.math.pow

enum class Type{
    Operand,
    Operator,
    Unary,
    LeftBracket,
    RightBracket
}
class Element constructor(val singleEl: String, val priority: Int,val type: Type )

fun calculate(expression:String):Float{
    val infixForm = expression.replace(" ","")//remove spaces
    if (infixForm.isEmpty())//check string for emptiness
        throw IllegalArgumentException("No expression")
    val parsedInf : List<Element> = parse(infixForm)//parsing an expression into its constituents
    check(parsedInf)//parsing an expression into its constituents
    val postfix: List<Element> = transferToPost(parsedInf)//postfix translation
    for(element in postfix) print(element.singleEl+" ")//postfix output
    return count(postfix)
}

fun check(parsedinf:List<Element>){
    for (i in 0..parsedinf.lastIndex){
        when(parsedinf[i].priority){
            0->{
                if (parsedinf[i].type == Type.RightBracket && i != parsedinf.lastIndex && parsedinf[i+1].type == Type.LeftBracket)//)(
                    throw IllegalArgumentException("Missing operator: ${parsedinf[i].singleEl}_${parsedinf[i+1].singleEl}")
                if (parsedinf[i].type == Type.LeftBracket && i != parsedinf.lastIndex && parsedinf[i+1].type == Type.Operator)//(*5)
                    throw IllegalArgumentException("Missing operand: ${parsedinf[i].singleEl}_${parsedinf[i+1].singleEl}")
                if (parsedinf[i].type==Type.LeftBracket && i!=parsedinf.lastIndex && parsedinf[i+1].type==Type.RightBracket)//()
                    throw IllegalArgumentException("Missing expression: ${parsedinf[i].singleEl}_${parsedinf[i+1].singleEl}")
            }
            1,2,3 ->{
                if (i==parsedinf.lastIndex)//5*
                    throw IllegalArgumentException("Missing operand: ${parsedinf[i].singleEl}_")
                else if (i==0)//*5
                    throw IllegalArgumentException("Missing operand: _${parsedinf[i].singleEl}")
                else if (parsedinf[i+1].type != Type.Operand && parsedinf[i+1].type != Type.LeftBracket && parsedinf[i+1].type != Type.Unary)//(5+5*)
                    throw IllegalArgumentException("Missing operand: ${parsedinf[i].singleEl}_${parsedinf[i+1].singleEl}")
            }
            4->{
                if (parsedinf[i+1].type != Type.Operand && parsedinf[i+1].type != Type.LeftBracket)//
                    throw IllegalArgumentException("Missing operand: ${parsedinf[i].singleEl}_${parsedinf[i+1].singleEl}")
            }
        }
    }
}

fun parse(infixform:String):List<Element>{
    val parsedinf : MutableList<Element> = arrayListOf()
    var i=0
    while (i <= infixform.lastIndex){
        if (infixform[i].isDigit()) {//number processing
            var number =""
            while (i <= infixform.lastIndex && infixform[i].isDigit()) {
                number+= infixform[i]
                i++
            }
            parsedinf.add(Element(number, -1,Type.Operand))
        }
        //handling unary plus and minus
        else if ((infixform[i] == '-' ||  infixform[i] == '+') && (i==0 || (!infixform[i-1].isDigit() && infixform[i-1] != ')')) && i != infixform.lastIndex){
            parsedinf.add(Element(infixform[i].toString()+"&", 4,Type.Unary))
            i++
        }
        else when(infixform[i]){
            '('->{
                parsedinf.add(Element(infixform[i].toString(), 0,Type.LeftBracket))
                i++
            }
            ')'-> {
                parsedinf.add(Element(infixform[i].toString(), 0,Type.RightBracket))
                i++
            }
            '+','-' ->{
                parsedinf.add(Element(infixform[i].toString(), 1,Type.Operator))
                i++
            }
            '*','/' ->{
                parsedinf.add(Element(infixform[i].toString(), 2,Type.Operator))
                i++
            }
            '^' ->{
                parsedinf.add(Element(infixform[i].toString(), 3,Type.Operator))
                i++
            }
            'e' ->{
                parsedinf.add(Element(E.toString(), -1,Type.Operand))
                i++
            }
            'p' ->{
                if (i != infixform.lastIndex && infixform[i+1]=='i')
                    parsedinf.add(Element(PI.toString(), -1,Type.Operand))
                else
                    throw IllegalArgumentException ("Unknown symbol: ${infixform[i]}")
                i+=2
            }
            else -> throw IllegalArgumentException ("Unknown symbol: ${infixform[i]}")
        }
    }
    return parsedinf
}
//postfix translation
fun transferToPost(parsedinf: List<Element>):MutableList<Element>{
    val stack:Stack<Element> = Stack()
    val postfix:MutableList<Element> = arrayListOf()
    for(element in parsedinf){
        when (element.type){
            Type.Operand ->{postfix.add(element)}
            Type.Operator ->{
                while(stack.isNotEmpty() && stack.peek().priority>=element.priority) {
                    postfix.add(stack.peek())
                    stack.pop()
                }
                stack.push(element)
            }
            Type.Unary ->{stack.push(element)}
            Type.LeftBracket ->{stack.push(element)}
            Type.RightBracket ->{
                while (stack.isNotEmpty()&&stack.peek().type!=Type.LeftBracket) {
                    postfix.add(stack.peek())
                    stack.pop()
                }
                if (stack.empty())
                    throw IllegalArgumentException ("brackets not matched")
                stack.pop()
            }
        }
    }
    while (stack.isNotEmpty()){
        if (stack.peek().priority==0)
            throw IllegalArgumentException ("brackets not matched")
        postfix.add(stack.peek())
        stack.pop()
    }
    return postfix
}

fun count(postfix:List<Element>):Float{
    val stack:Stack<Float> = Stack()
    for(element in postfix){
        if (element.type == Type.Operand)
            stack.push(element.singleEl.toFloat())
        else{
            val digit1 = stack.peek()
            stack.pop()
            var digit2 = 0.0F
            if (element.type !=Type.Unary) {
                digit2 = stack.peek()
                stack.pop()
            }
            when(element.singleEl){
                "+" -> { stack.push(digit2+digit1)}
                "-" -> { stack.push(digit2-digit1)}
                "*" -> { stack.push(digit2*digit1)}
                "/" -> { stack.push(digit2/digit1)}
                "^" -> { stack.push(digit2.pow(digit1))}
                "-&" ->{stack.push(digit1*-1)}
                "+&"->{stack.push(digit1)}
            }
        }
    }
    return stack.peek()//the result is the remaining item on the stack
}
