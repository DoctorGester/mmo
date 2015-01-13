import core.board.ControlType
import core.board.interfaces.Board
import core.board.interfaces.Buff
import core.board.interfaces.Unit

def onInit(Buff buff, Board board){
    if (buff.data instanceof Unit) {
        (buff.data as Unit).addBuff(buff)
        (buff.data as Unit).applyControl(ControlType.ROOT, buff)
    } else if (buff.data instanceof List<Unit>){
        for (Unit unit in (buff.data as List<Unit>)){
            unit.addBuff(buff)
            unit.applyControl(ControlType.ROOT, buff)
        }
    }
}

def onTick(Buff buff, Board board){}

def onEnd(Buff buff, Board board){
    if (buff.data instanceof Unit) {
        (buff.data as Unit).removeBuff(buff)
    } else if (buff.data instanceof List<Unit>){

        for (Unit unit in (buff.data as List<Unit>))
            unit.removeBuff(buff)

    }

}